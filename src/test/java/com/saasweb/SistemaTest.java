package com.saasweb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.dto.TenantAdminDtos.AdminInicial;
import com.saasweb.core.dto.TenantAdminDtos.TenantCreateRequest;
import com.saasweb.core.model.Rubro;
import com.saasweb.core.model.Sistema;
import com.saasweb.core.model.Tenant;
import com.saasweb.core.repository.RoleRepository;
import com.saasweb.core.service.PlanService;
import com.saasweb.core.service.TenantProvisioningService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Etapa 1c: el sistema de la tienda decide su plan, sus permisos y si tiene sitio público. */
@SpringBootTest
@AutoConfigureMockMvc
class SistemaTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired TenantProvisioningService provisioning;
    @Autowired PlanService planService;
    @Autowired RoleRepository roles;

    private Tenant alta(Sistema sistema, String planId) {
        String slug = "t-" + UUID.randomUUID().toString().substring(0, 8);
        return provisioning.provision(new TenantCreateRequest("Tienda " + sistema, slug, Rubro.ROPA,
                null, null, null, null, null, null, null, null, null, null, null, planId, sistema, null));
    }

    private List<String> permisosDelRol(Tenant t, String rol) {
        return new ArrayList<>(roles.findByTenantIdAndNameIgnoreCase(t.getId(), rol).orElseThrow().getPermissions());
    }

    @Test
    void cadaSistemaNaceConSuPlanYSusRoles() {
        Tenant pos = alta(Sistema.POS, null);
        assertThat(pos.getSistema()).isEqualTo(Sistema.POS);
        assertThat(pos.getPlanId()).isEqualTo(planService.ensurePosPlan().getId());
        assertThat(permisosDelRol(pos, "Administrador")).contains("POS_USE", "USERS_MANAGE")
                .doesNotContain("ORDERS_VIEW", "PLATFORM_SETTINGS_MANAGE");
        assertThat(roles.findByTenantIdAndNameIgnoreCase(pos.getId(), "Cajero")).isPresent();

        Tenant eco = alta(Sistema.ECOMMERCE, null);
        assertThat(permisosDelRol(eco, "Administrador")).contains("ORDERS_VIEW", "DOMAINS_MANAGE")
                .doesNotContain("CAROUSEL_MANAGE");
        assertThat(roles.findByTenantIdAndNameIgnoreCase(eco.getId(), "Vendedor")).isPresent();

        Tenant resto = alta(Sistema.RESTAURANTE, null);
        assertThat(resto.getPlanId()).isEqualTo(planService.ensureRestaurantePlan().getId());
        assertThat(permisosDelRol(resto, "Administrador")).contains("USERS_MANAGE").doesNotContain("POS_USE");
    }

    @Test
    void sinSistemaMandaElDelPlanYUnPlanDeOtroSistemaSeRechaza() {
        Tenant porPlan = alta(null, planService.ensurePosPlan().getId());
        assertThat(porPlan.getSistema()).isEqualTo(Sistema.POS);

        String planPos = planService.ensurePosPlan().getId();
        assertThatThrownBy(() -> alta(Sistema.RESTAURANTE, planPos))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("otro sistema");
    }

    @Test
    void unaTiendaPosNoTieneSitioPublico() throws Exception {
        Tenant pos = alta(Sistema.POS, null);
        // El catálogo no existe; la identidad sí (la usan el login y el front para mandar al panel).
        mvc.perform(get("/api/products").with(r -> { r.setServerName(pos.getSlug() + ".localhost"); return r; }))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("sin_sitio_online"));
        mvc.perform(get("/api/settings").with(r -> { r.setServerName(pos.getSlug() + ".localhost"); return r; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sistema").value("POS"));

        Tenant eco = alta(Sistema.ECOMMERCE, null);
        mvc.perform(get("/api/settings").with(r -> { r.setServerName(eco.getSlug() + ".localhost"); return r; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sistema").value("ECOMMERCE"))
                .andExpect(jsonPath("$.ecommerceSiteEnabled").value(true))
                .andExpect(jsonPath("$.posEnabled").value(false));
    }

    @Test
    void laTiendaNaceConSuAdminQueEntraPorSuDominio() throws Exception {
        String slug = "t-" + UUID.randomUUID().toString().substring(0, 8);
        String dni = String.valueOf(20000000 + (int) (Math.random() * 9_000_000));
        provisioning.provision(new TenantCreateRequest("Con admin", slug, Rubro.ROPA,
                null, null, null, null, null, null, null, null, null, null, null, null, Sistema.POS,
                new AdminInicial("Ana", "Pérez", dni, "ana@" + slug + ".com", "clave123")));

        String login = "{\"dni\":\"" + dni + "\",\"password\":\"clave123\"}";
        // Entra por el subdominio de SU tienda (el panel sigue andando aunque sea POS)...
        mvc.perform(post("/api/auth/login").with(r -> { r.setServerName(slug + ".localhost"); return r; })
                        .contentType(MediaType.APPLICATION_JSON).content(login))
                .andExpect(status().isOk());
        // ...pero ese DNI no existe en la tienda piloto.
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(login))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void elAdminSoloVeYAsignaPermisosDeSuSistema() throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dni\":\"11111111\",\"password\":\"test-pass\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String auth = "Bearer " + mapper.readTree(body).get("token").asText();

        JsonNode permisos = mapper.readTree(mvc.perform(get("/api/admin/permissions").header("Authorization", auth))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        List<String> claves = new ArrayList<>();
        permisos.forEach(p -> claves.add(p.get("key").asText()));
        assertThat(claves).contains("PRODUCTS_VIEW", "USERS_MANAGE")
                .doesNotContain("PLATFORM_SETTINGS_MANAGE", "CAROUSEL_MANAGE");

        mvc.perform(post("/api/admin/roles").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Intruso " + UUID.randomUUID() + "\",\"permissions\":[\"PLATFORM_SETTINGS_MANAGE\"]}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/admin/roles").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Depósito " + UUID.randomUUID() + "\",\"permissions\":[\"PRODUCTS_VIEW\"]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.permissions[0]").value("PRODUCTS_VIEW"));
    }
}
