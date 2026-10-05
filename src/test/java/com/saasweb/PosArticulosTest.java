package com.saasweb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saasweb.core.service.TenantProvisioningService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Etapa 3a: artículos, categorías y stock del POS, con tablas propias. */
@SpringBootTest
@AutoConfigureMockMvc
class PosArticulosTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired TenantProvisioningService provisioning;

    @Test
    void elKioscoNaceConArticulosDeEjemploYSeEncuentranPorCodigo() throws Exception {
        PosPrueba pos = PosPrueba.nuevo(provisioning, mvc, mapper);
        mvc.perform(pos.en(get("/api/pos/articulos"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(8)));
        mvc.perform(pos.en(get("/api/pos/categorias"))).andExpect(jsonPath("$.length()").value(3));
        mvc.perform(pos.en(get("/api/pos/articulos/codigo/7790895000997")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Agua mineral 500 ml"));
        mvc.perform(pos.en(get("/api/pos/articulos/codigo/000"))).andExpect(status().isNotFound());
    }

    @Test
    void altaCodigoUnicoIngresoAjusteYArchivo() throws Exception {
        PosPrueba pos = PosPrueba.nuevo(provisioning, mvc, mapper);
        JsonNode a = mapper.readTree(mvc.perform(pos.en(post("/api/pos/articulos")).content(
                        "{\"nombre\":\"Yerba 1 kg\",\"codigoBarras\":\"779000111\",\"precio\":4500,\"costo\":3000,\"stockInicial\":10,\"umbralStock\":3}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.stock").value(10))
                .andReturn().getResponse().getContentAsString());
        String id = a.get("id").asText();

        // El código ya es de otro artículo de la tienda.
        mvc.perform(pos.en(post("/api/pos/articulos")).content(
                        "{\"nombre\":\"Otro\",\"codigoBarras\":\"779000111\",\"precio\":1}"))
                .andExpect(status().isBadRequest());

        // Ingreso de mercadería (+5, actualiza el costo) y ajuste por conteo (quedan 12).
        mvc.perform(pos.en(post("/api/pos/articulos/" + id + "/stock")).content(
                        "{\"motivo\":\"INGRESO\",\"cantidad\":5,\"costoUnitario\":3200}"))
                .andExpect(jsonPath("$.stock").value(15)).andExpect(jsonPath("$.costo").value(3200));
        mvc.perform(pos.en(post("/api/pos/articulos/" + id + "/stock")).content(
                        "{\"motivo\":\"AJUSTE\",\"cantidad\":2,\"nota\":\"conteo\"}"))
                .andExpect(jsonPath("$.stock").value(2)).andExpect(jsonPath("$.stockBajo").value(true));
        mvc.perform(pos.en(post("/api/pos/articulos/" + id + "/stock")).content("{\"motivo\":\"VENTA\",\"cantidad\":1}"))
                .andExpect(status().isBadRequest());

        JsonNode movs = mapper.readTree(mvc.perform(pos.en(get("/api/pos/stock/movimientos?articuloId=" + id)))
                .andReturn().getResponse().getContentAsString());
        assertThat(movs).hasSize(3);
        assertThat(movs.get(0).get("cantidad").asInt()).isEqualTo(-13);
        assertThat(movs.get(0).get("stockResultante").asInt()).isEqualTo(2);
        assertThat(movs.get(0).get("autorNombre").asText()).isEqualTo("Caja Prueba");

        // Archivado: sale de la lista y del lector.
        mvc.perform(pos.en(post("/api/pos/articulos/" + id + "/archivar"))).andExpect(jsonPath("$.archivado").value(true));
        mvc.perform(pos.en(get("/api/pos/articulos/codigo/779000111"))).andExpect(status().isNotFound());
        mvc.perform(pos.en(get("/api/pos/articulos?archivados=true"))).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void unaCategoriaConArticulosNoSeBorra() throws Exception {
        PosPrueba pos = PosPrueba.nuevo(provisioning, mvc, mapper);
        String cat = mapper.readTree(mvc.perform(pos.en(post("/api/pos/categorias")).content("{\"nombre\":\"Limpieza\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asText();
        mvc.perform(pos.en(post("/api/pos/articulos")).content(
                "{\"nombre\":\"Lavandina\",\"precio\":900,\"categoriaId\":\"" + cat + "\"}")).andExpect(status().isCreated());
        mvc.perform(pos.en(delete("/api/pos/categorias/" + cat))).andExpect(status().isBadRequest());
    }

    @Test
    void elEcommerceNoVeLosArticulosDelPos() throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"dni\":\"11111111\",\"password\":\"test-pass\"}")).andReturn().getResponse().getContentAsString();
        String auth = "Bearer " + mapper.readTree(body).get("token").asText();
        // La tienda piloto (ecommerce) no tiene las URLs del POS.
        mvc.perform(get("/api/pos/articulos").header("Authorization", auth)).andExpect(status().isNotFound());
    }
}
