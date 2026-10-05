package com.saasweb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Etapa 2.4: lo nuevo del ecommerce de Ruth, del lado del backend. */
@SpringBootTest
@AutoConfigureMockMvc
class ParidadRuthTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private String login(String dni, String pass) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"dni\":\"" + dni + "\",\"password\":\"" + pass + "\"}")).andReturn().getResponse().getContentAsString();
        return "Bearer " + mapper.readTree(body).get("token").asText();
    }

    private JsonNode crearProducto(String auth, int precio, boolean enPromos) throws Exception {
        String json = "{\"name\":\"Paridad " + UUID.randomUUID().toString().substring(0, 6) + "\",\"description\":\"Producto de prueba\","
                + "\"price\":" + precio + ",\"images\":[\"https://example.com/a.jpg\"],\"featuredInPromos\":" + enPromos + ","
                + "\"sizeStocks\":[{\"size\":\"U\",\"stock\":5}]}";
        return mapper.readTree(mvc.perform(post("/api/admin/products").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
    }

    @Test
    void mostrarEnPromosAjusteMasivoYBorradoDefinitivo() throws Exception {
        String auth = login("11111111", "test-pass");
        JsonNode p = crearProducto(auth, 1000, true);
        String id = p.get("id").asText();
        mvc.perform(get("/api/admin/products/" + id).header("Authorization", auth))
                .andExpect(jsonPath("$.featuredInPromos").value(true));

        mvc.perform(patch("/api/admin/products/bulk-price").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"ids\":[\"" + id + "\"],\"percent\":10}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.updated").value(1));
        mvc.perform(get("/api/admin/products/" + id).header("Authorization", auth))
                .andExpect(jsonPath("$.price").value(1100));

        // Sin archivar no se puede borrar definitivo; archivado, sí.
        mvc.perform(delete("/api/admin/products/" + id + "/permanent").header("Authorization", auth))
                .andExpect(status().isBadRequest());
        mvc.perform(delete("/api/admin/products/" + id).header("Authorization", auth)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/admin/products/" + id + "/permanent").header("Authorization", auth))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/admin/products/" + id).header("Authorization", auth)).andExpect(status().isNotFound());
    }

    @Test
    void elCatalogoPublicoNoExponeDatosInternos() throws Exception {
        String auth = login("11111111", "test-pass");
        String json = "{\"name\":\"Interno " + UUID.randomUUID().toString().substring(0, 6) + "\",\"description\":\"Producto de prueba\","
                + "\"price\":500,\"costPrice\":200,\"images\":[\"https://example.com/1.jpg\",\"https://example.com/2.jpg\"],"
                + "\"barcode\":\"779" + System.nanoTime() % 1_000_000_000L + "\",\"sizeStocks\":[{\"size\":\"U\",\"stock\":5}]}";
        String id = mapper.readTree(mvc.perform(post("/api/admin/products").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asText();

        JsonNode lista = mapper.readTree(mvc.perform(get("/api/products")).andReturn().getResponse().getContentAsString());
        JsonNode enLista = null;
        for (JsonNode p : lista) if (p.get("id").asText().equals(id)) enLista = p;
        org.assertj.core.api.Assertions.assertThat(enLista).isNotNull();
        org.assertj.core.api.Assertions.assertThat(enLista.has("costPrice")).isFalse();
        org.assertj.core.api.Assertions.assertThat(enLista.has("supplierId")).isFalse();
        org.assertj.core.api.Assertions.assertThat(enLista.has("barcode")).isFalse();
        org.assertj.core.api.Assertions.assertThat(enLista.has("images")).isFalse();
        org.assertj.core.api.Assertions.assertThat(enLista.get("imageUrl").asText()).isEqualTo("https://example.com/1.jpg");

        mvc.perform(get("/api/products/" + id))
                .andExpect(jsonPath("$.images.length()").value(2))
                .andExpect(jsonPath("$.costPrice").doesNotExist())
                .andExpect(jsonPath("$.sizeStocks[0].barcode").doesNotExist());
    }

    @Test
    void cambioSinLlevarseNadaEsUnaDevolucionPura() throws Exception {
        String auth = login("11111111", "test-pass");
        JsonNode p = crearProducto(auth, 2000, false);
        mvc.perform(post("/api/admin/exchanges").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"returned\":[{\"productId\":\"" + p.get("id").asText() + "\",\"size\":\"U\",\"quantity\":1}],"
                                + "\"paymentMethod\":\"CASH\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.difference").value(-2000));
    }

    @Test
    void bannerPromocionalNosotrosYFotoDelLocal() throws Exception {
        String superadmin = login("33756194", "augusto123");
        JsonNode actual = mapper.readTree(mvc.perform(get("/api/settings")).andReturn().getResponse().getContentAsString());
        String body = "{\"storeName\":\"" + actual.get("storeName").asText() + "\",\"whatsappNumber\":\""
                + actual.get("whatsappNumber").asText() + "\",\"storePhotoUrl\":\"https://example.com/local.jpg\","
                + "\"aboutPageEnabled\":true,\"promoBannerEnabled\":true,\"promoBannerImage\":\"https://example.com/b.jpg\","
                + "\"promoBannerLink\":\"/promos\"}";
        mvc.perform(put("/api/admin/settings/platform").header("Authorization", superadmin)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        mvc.perform(get("/api/settings"))
                .andExpect(jsonPath("$.storePhotoUrl").value("https://example.com/local.jpg"))
                .andExpect(jsonPath("$.aboutPageEnabled").value(true))
                .andExpect(jsonPath("$.promoBannerEnabled").value(true))
                .andExpect(jsonPath("$.promoBannerLink").value("/promos"));
    }
}
