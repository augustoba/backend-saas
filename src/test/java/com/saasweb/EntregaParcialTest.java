package com.saasweb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Etapa 2.3: entrega y cancelación parcial de pedidos (portado del ecommerce de Ruth). */
@SpringBootTest
@AutoConfigureMockMvc
class EntregaParcialTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private String auth() throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"dni\":\"11111111\",\"password\":\"test-pass\"}")).andReturn().getResponse().getContentAsString();
        return "Bearer " + mapper.readTree(body).get("token").asText();
    }

    /** Dos variantes con stock (de productos distintos), como [productId, talle, stock]. */
    private List<String[]> dosVariantesConStock() throws Exception {
        JsonNode products = mapper.readTree(mvc.perform(get("/api/products")).andReturn().getResponse().getContentAsString());
        List<String[]> out = new ArrayList<>();
        for (JsonNode p : products) {
            for (JsonNode s : p.get("sizeStocks")) {
                if (s.get("stock").asInt() >= 2) {
                    out.add(new String[]{p.get("id").asText(), s.get("size").asText(), s.get("stock").asText()});
                    break;
                }
            }
            if (out.size() == 2) return out;
        }
        throw new IllegalStateException("El seed no tiene dos productos con stock");
    }

    private JsonNode crearPedido(List<String[]> items) throws Exception {
        StringBuilder json = new StringBuilder("{\"customerName\":\"Entrega parcial\",\"items\":[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) json.append(',');
            json.append("{\"productId\":\"").append(items.get(i)[0]).append("\",\"size\":\"")
                    .append(items.get(i)[1]).append("\",\"quantity\":1}");
        }
        json.append("]}");
        return mapper.readTree(mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(json.toString()))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
    }

    private int stockDe(String productId, String talle) throws Exception {
        JsonNode p = mapper.readTree(mvc.perform(get("/api/products/" + productId)).andReturn().getResponse().getContentAsString());
        for (JsonNode s : p.get("sizeStocks")) if (s.get("size").asText().equals(talle)) return s.get("stock").asInt();
        return -1;
    }

    @Test
    void entregarUnaLineaYCancelarLaOtraResuelveElPedido() throws Exception {
        String auth = auth();
        List<String[]> v = dosVariantesConStock();
        int stockAntes = stockDe(v.get(0)[0], v.get(0)[1]);
        JsonNode pedido = crearPedido(v);
        String id = pedido.get("id").asText();
        String linea1 = pedido.get("lines").get(0).get("id").asText();
        String linea2 = pedido.get("lines").get(1).get("id").asText();

        // Entrega sólo la primera: el pedido sigue pendiente y baja ese stock.
        mvc.perform(post("/api/admin/orders/" + id + "/confirm-lines").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"lineIds\":[\"" + linea1 + "\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDIENTE"))
                .andExpect(jsonPath("$.lines[0].status").value("ENTREGADA"))
                .andExpect(jsonPath("$.lines[1].status").value("PENDIENTE"));
        assertThat(stockDe(v.get(0)[0], v.get(0)[1])).isEqualTo(stockAntes - 1);

        // Una línea ya entregada no se puede volver a tocar.
        mvc.perform(post("/api/admin/orders/" + id + "/cancel-lines").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"lineIds\":[\"" + linea1 + "\"]}"))
                .andExpect(status().isBadRequest());

        // Cancelar la otra resuelve el pedido como PROCESADO y deja de cobrarla.
        JsonNode resuelto = mapper.readTree(mvc.perform(post("/api/admin/orders/" + id + "/cancel-lines")
                        .header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"lineIds\":[\"" + linea2 + "\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESADO"))
                .andExpect(jsonPath("$.lines[1].status").value("CANCELADA"))
                .andReturn().getResponse().getContentAsString());
        assertThat(resuelto.get("subtotal").decimalValue())
                .isEqualByComparingTo(pedido.get("lines").get(0).get("unitPrice").decimalValue());
    }

    @Test
    void unPedidoPendienteSeEditaYCancelarloTodoNoTocaStock() throws Exception {
        String auth = auth();
        List<String[]> v = dosVariantesConStock();
        int stockAntes = stockDe(v.get(1)[0], v.get(1)[1]);
        JsonNode pedido = crearPedido(List.<String[]>of(v.get(0)));
        String id = pedido.get("id").asText();

        JsonNode conDos = mapper.readTree(mvc.perform(post("/api/admin/orders/" + id + "/lines").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + v.get(1)[0] + "\",\"size\":\"" + v.get(1)[1] + "\",\"quantity\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines.length()").value(2))
                .andReturn().getResponse().getContentAsString());
        String nueva = conDos.get("lines").get(1).get("id").asText();

        mvc.perform(patch("/api/admin/orders/" + id + "/lines/" + nueva).header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines[1].quantity").value(2));

        mvc.perform(delete("/api/admin/orders/" + id + "/lines/" + nueva).header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines.length()").value(1));
        // No se puede dejar un pedido sin ítems.
        String unica = pedido.get("lines").get(0).get("id").asText();
        mvc.perform(delete("/api/admin/orders/" + id + "/lines/" + unica).header("Authorization", auth))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/admin/orders/" + id + "/cancel").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"))
                .andExpect(jsonPath("$.lines[0].status").value("CANCELADA"));
        assertThat(stockDe(v.get(1)[0], v.get(1)[1])).isEqualTo(stockAntes);
    }
}
