package com.saasweb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saasweb.core.model.Sistema;
import com.saasweb.core.repository.TenantRepository;
import com.saasweb.core.service.TenantDeletionService;
import com.saasweb.core.service.TenantProvisioningService;
import com.saasweb.restaurante.repository.OrdenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Etapa 4a: el restaurante portado funciona sobre el núcleo (tienda, usuarios, dominio). */
@SpringBootTest
@AutoConfigureMockMvc
class RestauranteTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired TenantProvisioningService provisioning;
    @Autowired TenantRepository tenants;
    @Autowired OrdenRepository ordenes;
    @Autowired TenantDeletionService bajas;

    private JsonNode json(ResultActions r) throws Exception {
        return mapper.readTree(r.andReturn().getResponse().getContentAsString());
    }

    @Test
    void mesaPedidoCocinaCobroCartaYBaja() throws Exception {
        PosPrueba resto = PosPrueba.nuevo(provisioning, mvc, mapper, Sistema.RESTAURANTE);
        String tenantId = tenants.findBySlug(resto.slug()).orElseThrow().getId();

        // El alta sembró salones con mesas, carta, medios de pago y cajas.
        JsonNode salones = json(mvc.perform(resto.en(get("/api/restaurante/salones"))).andExpect(status().isOk()));
        long mesaId = salones.get(0).get("mesas").get(0).get("id").asLong();
        JsonNode productos = json(mvc.perform(resto.en(get("/api/restaurante/productos"))).andExpect(status().isOk()));
        JsonNode producto = null;
        for (JsonNode p : productos.has("content") ? productos.get("content") : productos) {
            if ("VENDIBLE".equals(p.get("tipo").asText()) && p.get("precio").decimalValue().signum() > 0) { producto = p; break; }
        }
        assertThat(producto).isNotNull();
        long efectivo = 0;
        for (JsonNode m : json(mvc.perform(resto.en(get("/api/restaurante/medios-pago"))))) {
            if (m.get("esEfectivo").asBoolean()) efectivo = m.get("id").asLong();
        }
        long cajaId = json(mvc.perform(resto.en(get("/api/restaurante/cajas")))).get(0).get("id").asLong();

        // Caja (arqueo) → mesa → pedido → cocina → cobro.
        mvc.perform(resto.en(post("/api/restaurante/arqueos")).content("{\"cajaId\":" + cajaId + ",\"montoApertura\":1000}"))
                .andExpect(status().isOk());
        JsonNode arqueo = json(mvc.perform(resto.en(get("/api/restaurante/arqueos/abiertos")))).get(0);
        assertThat(arqueo.get("usuarioAperturaNombre").asText()).isEqualTo("Caja Prueba");

        JsonNode orden = json(mvc.perform(resto.en(post("/api/restaurante/ordenes/mesa")).content(
                "{\"mesaId\":" + mesaId + ",\"comensales\":2,\"arqueoId\":" + arqueo.get("id").asLong() + "}")).andExpect(status().isOk()));
        long ordenId = orden.get("id").asLong();
        assertThat(orden.get("usuarioNombre").asText()).isEqualTo("Caja Prueba");
        mvc.perform(resto.en(post("/api/restaurante/ordenes/" + ordenId + "/items")).content(
                "{\"productoId\":" + producto.get("id").asLong() + ",\"cantidad\":2}")).andExpect(status().isOk());
        JsonNode confirmada = json(mvc.perform(resto.en(post("/api/restaurante/ordenes/" + ordenId + "/confirmar"))).andExpect(status().isOk()));
        assertThat(json(mvc.perform(resto.en(get("/api/restaurante/kds/pendientes"))).andExpect(status().isOk())).toString())
                .contains(producto.get("nombre").asText());

        BigDecimal total = confirmada.get("total").decimalValue();
        assertThat(total).isEqualByComparingTo(producto.get("precio").decimalValue().multiply(BigDecimal.valueOf(2)));
        JsonNode pagada = json(mvc.perform(resto.en(post("/api/restaurante/ordenes/" + ordenId + "/pagos")).content(
                "{\"medioPagoId\":" + efectivo + ",\"monto\":" + total + "}")).andExpect(status().isOk()));
        assertThat(pagada.get("estado").asText()).isEqualTo("PAGADO");

        // Carta pública: por el dominio del restaurante sí; desde otra tienda, no existe.
        String slug = json(mvc.perform(resto.en(get("/api/restaurante/menu-online/config")))).get("slug").asText();
        mvc.perform(get("/api/public/restaurante/carta/" + slug).with(r -> { r.setServerName(resto.slug() + ".localhost"); return r; }))
                .andExpect(status().isOk());
        mvc.perform(get("/api/public/restaurante/carta/" + slug)).andExpect(status().isNotFound());
        // La dirección del restaurante sabe cuál es su carta (el front manda "/" ahí).
        assertThat(json(mvc.perform(get("/api/public/restaurante/carta")
                .with(r -> { r.setServerName(resto.slug() + ".localhost"); return r; }))).get("slug").asText()).isEqualTo(slug);

        // Otra tienda no ve los datos del restaurante.
        String body = mvc.perform(post("/api/auth/login").contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"dni\":\"11111111\",\"password\":\"test-pass\"}")).andReturn().getResponse().getContentAsString();
        String piloto = "Bearer " + mapper.readTree(body).get("token").asText();
        mvc.perform(get("/api/restaurante/ordenes/" + ordenId).header("Authorization", piloto)).andExpect(status().is4xxClientError());

        // La baja se lleva todo.
        bajas.deleteTenant(tenantId, resto.slug());
        assertThat(ordenes.findById(ordenId)).isEmpty();
    }
}
