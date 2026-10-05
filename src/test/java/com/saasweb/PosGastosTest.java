package com.saasweb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saasweb.core.service.TenantProvisioningService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Etapa 3c: gastos y proveedores del POS, y el reporte de resultados. */
@SpringBootTest
@AutoConfigureMockMvc
class PosGastosTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired TenantProvisioningService provisioning;

    private JsonNode json(ResultActions r) throws Exception {
        return mapper.readTree(r.andReturn().getResponse().getContentAsString());
    }

    @Test
    void gastoDeLaCajaGeneraEgresoYElReporteDaLaGanancia() throws Exception {
        PosPrueba pos = PosPrueba.nuevo(provisioning, mvc, mapper);
        String prov = json(mvc.perform(pos.en(post("/api/pos/proveedores")).content("{\"nombre\":\"Distribuidora Sur\"}"))
                .andExpect(status().isCreated())).get("id").asText();

        // Pagado de la caja sin caja abierta: no se puede.
        String gastoCaja = "{\"concepto\":\"Flete\",\"categoria\":\"Mercadería\",\"monto\":2000,\"proveedorId\":\"" + prov
                + "\",\"pagadoDeCaja\":true}";
        mvc.perform(pos.en(post("/api/pos/gastos")).content(gastoCaja)).andExpect(status().isBadRequest());

        mvc.perform(pos.en(post("/api/pos/caja/abrir")).content("{\"montoInicial\":5000}"));
        String g1 = json(mvc.perform(pos.en(post("/api/pos/gastos")).content(gastoCaja)).andExpect(status().isCreated()))
                .get("id").asText();
        mvc.perform(pos.en(post("/api/pos/gastos")).content("{\"concepto\":\"Luz\",\"categoria\":\"Servicios\",\"monto\":8000}"))
                .andExpect(status().isCreated());
        assertThat(json(mvc.perform(pos.en(get("/api/pos/caja/actual")))).get("egresos").decimalValue())
                .isEqualByComparingTo("2000");

        // Venta: 2 aguas (precio 900, costo 520) + un importe suelto de 500 (sin costo).
        String agua = json(mvc.perform(pos.en(get("/api/pos/articulos/codigo/7790895000997")))).get("id").asText();
        mvc.perform(pos.en(post("/api/pos/ventas")).content("{\"items\":[{\"articuloId\":\"" + agua
                + "\",\"cantidad\":2},{\"cantidad\":1,\"precio\":500}],\"medioPago\":\"EFECTIVO\"}")).andExpect(status().isCreated());

        JsonNode r = json(mvc.perform(pos.en(get("/api/pos/reporte"))).andExpect(status().isOk()));
        assertThat(r.get("totalVendido").decimalValue()).isEqualByComparingTo("2300");
        assertThat(r.get("costoMercaderia").decimalValue()).isEqualByComparingTo("1040");
        assertThat(r.get("vendidoSinCosto").decimalValue()).isEqualByComparingTo("500");
        assertThat(r.get("gananciaBruta").decimalValue()).isEqualByComparingTo("1260");
        assertThat(r.get("gastos").decimalValue()).isEqualByComparingTo("10000");
        assertThat(r.get("resultado").decimalValue()).isEqualByComparingTo("-8740");
        assertThat(r.get("gastosPorCategoria").get("Servicios").decimalValue()).isEqualByComparingTo("8000");
        assertThat(r.get("masVendidos").get(0).get("unidades").asInt()).isEqualTo(2);

        // El proveedor con gastos no se borra; el gasto de la caja abierta sí (y se lleva su egreso).
        mvc.perform(pos.en(delete("/api/pos/proveedores/" + prov))).andExpect(status().isBadRequest());
        mvc.perform(pos.en(delete("/api/pos/gastos/" + g1))).andExpect(status().isNoContent());
        assertThat(json(mvc.perform(pos.en(get("/api/pos/caja/actual")))).get("egresos").decimalValue())
                .isEqualByComparingTo("0");
        assertThat(json(mvc.perform(pos.en(get("/api/pos/gastos")))).size()).isEqualTo(1);
    }
}
