package com.saasweb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saasweb.core.dto.TenantAdminDtos.AdminInicial;
import com.saasweb.core.dto.TenantAdminDtos.TenantCreateRequest;
import com.saasweb.core.model.Rubro;
import com.saasweb.core.model.Sistema;
import com.saasweb.core.service.TenantProvisioningService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** Una tienda nueva de un sistema para un test (POS por defecto): su subdominio y el token de su administrador. */
record PosPrueba(String slug, String token, MockMvc mvc) {

    static PosPrueba nuevo(TenantProvisioningService provisioning, MockMvc mvc, ObjectMapper mapper) throws Exception {
        return nuevo(provisioning, mvc, mapper, Sistema.POS);
    }

    static PosPrueba nuevo(TenantProvisioningService provisioning, MockMvc mvc, ObjectMapper mapper, Sistema sistema) throws Exception {
        String slug = sistema.name().toLowerCase().substring(0, 3) + "-" + UUID.randomUUID().toString().substring(0, 8);
        String dni = String.valueOf(20000000 + (int) (Math.random() * 9_000_000));
        provisioning.provision(new TenantCreateRequest("Kiosco " + slug, slug, Rubro.ROPA,
                null, null, null, null, null, null, null, null, null, null, null, null, sistema,
                new AdminInicial("Caja", "Prueba", dni, "caja@" + slug + ".com", "clave123")));
        RequestPostProcessor host = r -> { r.setServerName(slug + ".localhost"); return r; };
        String body = mvc.perform(post("/api/auth/login").with(host).contentType(MediaType.APPLICATION_JSON)
                .content("{\"dni\":\"" + dni + "\",\"password\":\"clave123\"}")).andReturn().getResponse().getContentAsString();
        return new PosPrueba(slug, "Bearer " + mapper.readTree(body).get("token").asText(), mvc);
    }

    /** La request entra por el subdominio del kiosco, autenticada y con JSON. */
    MockHttpServletRequestBuilder en(MockHttpServletRequestBuilder req) {
        return req.with(r -> { r.setServerName(slug + ".localhost"); return r; })
                .header("Authorization", token).contentType(MediaType.APPLICATION_JSON);
    }
}
