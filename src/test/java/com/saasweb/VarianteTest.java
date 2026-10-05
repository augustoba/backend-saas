package com.saasweb;

import com.saasweb.ecommerce.model.Product;
import com.saasweb.ecommerce.model.Variante;
import com.saasweb.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Etapa 2.2b: las variantes tienen identidad propia y editar el producto no las recrea. */
@SpringBootTest
@AutoConfigureMockMvc
class VarianteTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Autowired ProductRepository products;
    @Autowired TransactionTemplate tx;

    @Test
    void editarLasVariantesConservaIdYCodigoDeBarras() {
        String id = UUID.randomUUID().toString();
        String[] idM = new String[1];
        tx.executeWithoutResult(s -> {
            Product p = new Product();
            p.setId(id);
            p.setTenantId("t-" + id);
            p.setName("Remera");
            p.setDescription("Remera de algodón");
            p.setPrice(new BigDecimal("1000"));
            Variante m = new Variante("M", 3);
            m.setCodigoBarras("7790001112223");
            p.getSizeStocks().addAll(List.of(new Variante("S", 2), m));
            products.save(p);
            idM[0] = m.getId();
        });

        tx.executeWithoutResult(s -> {
            Product p = products.findById(id).orElseThrow();
            // Se va S, M cambia de stock y pasa primera, aparece XL.
            Variante m = new Variante("M", 7);
            m.setCodigoBarras("7790001112223"); // el formulario manda siempre el código actual
            p.reemplazarVariantes(List.of(m, new Variante("XL", 1)));
            products.save(p);
        });

        tx.executeWithoutResult(s -> {
            List<Variante> v = products.findById(id).orElseThrow().getSizeStocks();
            assertThat(v).extracting(Variante::getNombre).containsExactly("M", "XL");
            assertThat(v.get(0).getId()).isEqualTo(idM[0]);
            assertThat(v.get(0).getCodigoBarras()).isEqualTo("7790001112223");
            assertThat(v.get(0).getStock()).isEqualTo(7);
        });
    }

    private String producto(String nombre, String variantesJson) {
        return "{\"name\":\"" + nombre + "\",\"description\":\"Producto de prueba\",\"price\":100,\"images\":[\"https://example.com/foto.jpg\"],"
                + "\"sizeStocks\":" + variantesJson + "}";
    }

    @Test
    void codigoDeBarrasPorVarianteUnicoYEncontrableAlEscanear() throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dni\":\"11111111\",\"password\":\"test-pass\"}"))
                .andReturn().getResponse().getContentAsString();
        String auth = "Bearer " + mapper.readTree(body).get("token").asText();
        String sufijo = UUID.randomUUID().toString().substring(0, 8);
        String codigoM = "VM" + sufijo;

        mvc.perform(post("/api/admin/products").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content(producto("Buzo " + sufijo, "[{\"size\":\"S\",\"stock\":1},{\"size\":\"M\",\"stock\":2,\"barcode\":\"" + codigoM + "\"}]")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sizeStocks[1].barcode").value(codigoM))
                .andExpect(jsonPath("$.sizeStocks[1].id").isNotEmpty());

        // Escanear el código de la variante encuentra el producto.
        mvc.perform(get("/api/admin/products/by-barcode").param("code", codigoM).header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Buzo " + sufijo));

        // El mismo código en otro producto, o repetido dentro del mismo, se rechaza.
        mvc.perform(post("/api/admin/products").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content(producto("Otro " + sufijo, "[{\"size\":\"U\",\"stock\":1,\"barcode\":\"" + codigoM + "\"}]")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/admin/products").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content(producto("Doble " + sufijo, "[{\"size\":\"A\",\"stock\":1,\"barcode\":\"X" + sufijo + "\"},{\"size\":\"B\",\"stock\":1,\"barcode\":\"X" + sufijo + "\"}]")))
                .andExpect(status().isBadRequest());
    }
}
