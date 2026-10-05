package com.saasweb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saasweb.core.config.AppProperties;
import com.saasweb.core.model.Dominio;
import com.saasweb.core.model.EstadoDominio;
import com.saasweb.core.model.Rubro;
import com.saasweb.core.model.Tenant;
import com.saasweb.core.repository.DominioRepository;
import com.saasweb.core.service.DominioService;
import com.saasweb.core.service.TenantResolver;
import com.saasweb.core.service.TenantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Resolución de tenant por Host (subdominio, dominio propio, raíz de la
 * plataforma) y el ABM de dominios. En los tests la plataforma es
 * `localhost`, igual que en desarrollo.
 */
@SpringBootTest
@AutoConfigureMockMvc
class DominioTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired TenantService tenantService;
    @Autowired TenantResolver resolver;
    @Autowired DominioRepository dominios;
    @Autowired AppProperties props;
    @Autowired DominioService dominioService;

    private Tenant otraTienda() {
        String slug = "otra-" + UUID.randomUUID().toString().substring(0, 8);
        return tenantService.create("Otra tienda", slug, Rubro.ROPA, null);
    }

    private String hostAlAzar() {
        return "tienda-" + UUID.randomUUID().toString().substring(0, 8) + ".com.ar";
    }

    private String loginAdmin() throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dni\":\"11111111\",\"password\":\"test-pass\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + mapper.readTree(body).get("token").asText();
    }

    @Test
    void elSubdominioResuelveSuTienda() {
        Tenant t = otraTienda();
        assertThat(resolver.resolve(t.getSlug() + ".localhost", null)).contains(t.getId());
        assertThat(resolver.resolve(t.getSlug() + ".localhost:4200", null)).contains(t.getId());
        assertThat(resolver.resolve("no-existe.localhost", null)).isEmpty();
    }

    @Test
    void laRaizEsLaTiendaPorDefectoSalvoElSelectorDeDemo() {
        String porDefecto = tenantService.resolveIdBySlug(props.getTenant().getSlug());
        Tenant t = otraTienda();
        assertThat(resolver.resolve("localhost", null)).contains(porDefecto);
        assertThat(resolver.resolve("localhost", t.getSlug())).contains(t.getId());
        // El header de demo no sirve para saltar de una tienda a otra desde un dominio de tienda.
        assertThat(resolver.resolve(t.getSlug() + ".localhost", "estilos-pequenos")).contains(t.getId());
    }

    @Test
    void unDominioPropioSoloResuelveCuandoEstaActivo() {
        Tenant t = otraTienda();
        Dominio d = new Dominio();
        d.setId(UUID.randomUUID().toString());
        d.setTenantId(t.getId());
        d.setHost(hostAlAzar());
        dominios.save(d);
        assertThat(resolver.resolve(d.getHost(), null)).isEmpty();

        d.setEstado(EstadoDominio.ACTIVO);
        dominios.save(d);
        assertThat(resolver.resolve(d.getHost().toUpperCase(), null)).contains(t.getId());
    }

    @Test
    void unHostDesconocidoRecibe404() throws Exception {
        mvc.perform(get("/api/settings").with(r -> { r.setServerName("nadie.com"); return r; }))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("tienda_no_encontrada"));
    }

    @Test
    void caddySoloPuedePedirCertificadoParaTiendasActivas() throws Exception {
        Tenant t = otraTienda();
        // La consulta llega desde Caddy con cualquier Host: no depende de una tienda.
        mvc.perform(get("/api/public/core/dominios/permitido").param("domain", t.getSlug() + ".localhost")
                        .with(r -> { r.setServerName("caddy.interno"); return r; }))
                .andExpect(status().isOk());
        mvc.perform(get("/api/public/core/dominios/permitido").param("domain", hostAlAzar()))
                .andExpect(status().isNotFound());

        tenantService.setActive(t.getId(), false);
        mvc.perform(get("/api/public/core/dominios/permitido").param("domain", t.getSlug() + ".localhost"))
                .andExpect(status().isNotFound());
    }

    @Test
    void altaYVerificacionDeUnDominioPropio() throws Exception {
        String auth = loginAdmin();
        String host = hostAlAzar();

        String body = mvc.perform(post("/api/core/dominios").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"host\":\"" + host.toUpperCase() + ":443\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.host").value(host))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andReturn().getResponse().getContentAsString();
        String id = mapper.readTree(body).get("id").asText();

        // Repetido o subdominio de la plataforma: rechazado.
        mvc.perform(post("/api/core/dominios").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                .content("{\"host\":\"" + host + "\"}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/core/dominios").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                .content("{\"host\":\"algo.localhost\"}")).andExpect(status().isBadRequest());

        // Con IP de servidor configurada, el DNS tiene que apuntar ahí. El DNS se
        // reemplaza en el bean (sin @MockBean: un contexto nuevo resetearía la
        // base H2 compartida con los otros tests).
        Object target = AopTestUtils.getTargetObject(dominioService);
        Object dnsReal = ReflectionTestUtils.getField(target, "dns");
        List<String> respuesta = new java.util.ArrayList<>(List.of("198.51.100.7"));
        ReflectionTestUtils.setField(target, "dns", new DominioService.DnsLookup() {
            @Override
            public List<String> resolve(String h) {
                return h.equals(host) ? respuesta : List.of();
            }
        });
        String ipAnterior = props.getPlatform().getServerIp();
        props.getPlatform().setServerIp("203.0.113.10");
        try {
            mvc.perform(post("/api/core/dominios/" + id + "/verificar").header("Authorization", auth))
                    .andExpect(status().isBadRequest());

            respuesta.set(0, "203.0.113.10");
            mvc.perform(post("/api/core/dominios/" + id + "/verificar").header("Authorization", auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado").value("ACTIVO"));
        } finally {
            props.getPlatform().setServerIp(ipAnterior);
            ReflectionTestUtils.setField(target, "dns", dnsReal);
        }

        String porDefecto = tenantService.resolveIdBySlug(props.getTenant().getSlug());
        assertThat(resolver.resolve(host, null)).contains(porDefecto);

        JsonNode lista = mapper.readTree(mvc.perform(get("/api/core/dominios").header("Authorization", auth))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(lista.get("subdominio").asText()).isEqualTo(props.getTenant().getSlug() + ".localhost");
    }
}
