package com.saasweb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saasweb.core.model.ConfigArca;
import com.saasweb.core.repository.ConfigArcaRepository;
import com.saasweb.core.repository.TenantRepository;
import com.saasweb.core.service.ArcaInvoiceService;
import com.saasweb.core.service.ArcaInvoiceService.InvoiceResult;
import com.saasweb.core.service.TenantDeletionService;
import com.saasweb.core.service.TenantProvisioningService;
import com.saasweb.pos.repository.ArticuloRepository;
import com.saasweb.pos.repository.VentaRepository;
import com.saasweb.pos.service.VentaService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Etapa 3b: caja (turno con arqueo) y ventas del POS, con factura de ARCA por el núcleo. */
@SpringBootTest
@AutoConfigureMockMvc
class PosVentasTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired TenantProvisioningService provisioning;
    @Autowired TenantRepository tenants;
    @Autowired ArticuloRepository articulos;
    @Autowired VentaRepository ventas;
    @Autowired ConfigArcaRepository configArca;
    @Autowired VentaService ventaService;
    @Autowired TenantDeletionService bajas;

    private JsonNode json(org.springframework.test.web.servlet.ResultActions r) throws Exception {
        return mapper.readTree(r.andReturn().getResponse().getContentAsString());
    }

    private String articulo(PosPrueba pos, String codigo) throws Exception {
        return json(mvc.perform(pos.en(get("/api/pos/articulos/codigo/" + codigo)))).get("id").asText();
    }

    private int stock(PosPrueba pos, String id) throws Exception {
        return json(mvc.perform(pos.en(get("/api/pos/articulos/" + id)))).get("stock").asInt();
    }

    @Test
    void turnoCompletoAbrirVenderAnularYCerrarConArqueo() throws Exception {
        PosPrueba pos = PosPrueba.nuevo(provisioning, mvc, mapper);
        String agua = articulo(pos, "7790895000997");      // $900, stock 24
        String alfajor = articulo(pos, "7790040113003");   // $1200, stock 30
        String venta = "{\"items\":[{\"articuloId\":\"" + agua + "\",\"cantidad\":2},{\"articuloId\":\"" + alfajor
                + "\",\"cantidad\":1}],\"medioPago\":\"EFECTIVO\",\"montoEntregado\":5000}";

        // Sin caja abierta no se vende.
        mvc.perform(pos.en(post("/api/pos/ventas")).content(venta)).andExpect(status().isBadRequest());
        mvc.perform(pos.en(get("/api/pos/caja/actual"))).andExpect(status().isNoContent());

        mvc.perform(pos.en(post("/api/pos/caja/abrir")).content("{\"montoInicial\":10000}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.caja.numero").value(1));
        mvc.perform(pos.en(post("/api/pos/caja/abrir")).content("{\"montoInicial\":0}")).andExpect(status().isBadRequest());

        // Efectivo con vuelto: 2×900 + 1200 = 3000; entrega 5000 → vuelto 2000.
        JsonNode v1 = json(mvc.perform(pos.en(post("/api/pos/ventas")).content(venta)).andExpect(status().isCreated()));
        assertThat(v1.get("codigo").asText()).isEqualTo("V-0001");
        assertThat(v1.get("total").decimalValue()).isEqualByComparingTo("3000");
        assertThat(v1.get("vuelto").decimalValue()).isEqualByComparingTo("2000");
        assertThat(v1.get("factura").isNull()).isTrue(); // ticket interno
        assertThat(stock(pos, agua)).isEqualTo(22);
        assertThat(stock(pos, alfajor)).isEqualTo(29);

        // Tarjeta + un importe suelto ("varios", no mueve stock).
        mvc.perform(pos.en(post("/api/pos/ventas")).content("{\"items\":[{\"articuloId\":\"" + agua
                        + "\",\"cantidad\":1},{\"cantidad\":1,\"nombre\":\"Carga SUBE\",\"precio\":2000}],"
                        + "\"medioPago\":\"TARJETA\",\"referencia\":\"0042\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.total").value(2900))
                .andExpect(jsonPath("$.lineas[1].nombre").value("Carga SUBE"));
        // El efectivo entregado no alcanza.
        mvc.perform(pos.en(post("/api/pos/ventas")).content("{\"items\":[{\"articuloId\":\"" + agua
                + "\",\"cantidad\":1}],\"medioPago\":\"EFECTIVO\",\"montoEntregado\":100}")).andExpect(status().isBadRequest());

        // Retiro de efectivo y anulación de la primera venta (repone stock).
        mvc.perform(pos.en(post("/api/pos/caja/movimientos")).content(
                "{\"tipo\":\"EGRESO\",\"monto\":1500,\"motivo\":\"Pago al repartidor\"}")).andExpect(status().isCreated());
        JsonNode v3 = json(mvc.perform(pos.en(post("/api/pos/ventas")).content(venta)));
        mvc.perform(pos.en(post("/api/pos/ventas/" + v3.get("id").asText() + "/anular")).content("{\"motivo\":\"se arrepintió\"}"))
                .andExpect(jsonPath("$.estado").value("ANULADA"));
        assertThat(stock(pos, agua)).isEqualTo(21);

        // Esperado = 10000 + 3000 (efectivo) − 1500 = 11500. Se cuentan 11400 → faltan 100.
        JsonNode resumen = json(mvc.perform(pos.en(get("/api/pos/caja/actual"))));
        assertThat(resumen.get("cantidadVentas").asInt()).isEqualTo(2);
        assertThat(resumen.get("ventasAnuladas").asInt()).isEqualTo(1);
        assertThat(resumen.get("porMedioPago").get("TARJETA").decimalValue()).isEqualByComparingTo("2900");
        assertThat(resumen.get("efectivoEsperado").decimalValue()).isEqualByComparingTo("11500");

        JsonNode cierre = json(mvc.perform(pos.en(post("/api/pos/caja/cerrar")).content("{\"efectivoContado\":11400}"))
                .andExpect(status().isOk()));
        assertThat(cierre.get("caja").get("estado").asText()).isEqualTo("CERRADA");
        assertThat(cierre.get("caja").get("diferencia").decimalValue()).isEqualByComparingTo("-100");

        // Cerrada: ni se vende ni se anula.
        mvc.perform(pos.en(post("/api/pos/ventas")).content(venta)).andExpect(status().isBadRequest());
        mvc.perform(pos.en(post("/api/pos/ventas/" + v1.get("id").asText() + "/anular")).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(pos.en(get("/api/pos/cajas"))).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void conArcaLaVentaSeFacturaYAlAnularseEmiteNotaDeCredito() throws Exception {
        PosPrueba pos = PosPrueba.nuevo(provisioning, mvc, mapper);
        String tenantId = tenants.findBySlug(pos.slug()).orElseThrow().getId();
        ConfigArca cfg = configArca.findById(tenantId).orElseGet(() -> { ConfigArca c = new ConfigArca(); c.setId(tenantId); return c; });
        cfg.setInvoiceMode("FACTURA_ARCA");
        configArca.save(cfg);

        ArcaInvoiceService arca = Mockito.mock(ArcaInvoiceService.class);
        Mockito.when(arca.isAvailable()).thenReturn(true);
        Mockito.when(arca.emitirFactura(eq(tenantId), anyList(), isNull(), isNull()))
                .thenReturn(new InvoiceResult(false, "FACTURA_C", null, null, null, null, null, "ARCA no responde"))
                .thenReturn(new InvoiceResult(true, "FACTURA_C", "75123456789012", "20261015", 7L, 3, "https://qr", null));
        Mockito.when(arca.emitirNotaCredito(eq(tenantId), eq("FACTURA_C"), eq(3), eq(7L), isNull(), any(BigDecimal.class)))
                .thenReturn(new InvoiceResult(true, "NC_C", "75123456789099", "20261015", 2L, 3, "https://qr2", null));
        Object target = AopTestUtils.getTargetObject(ventaService);
        Object original = ReflectionTestUtils.getField(target, "arca");
        ReflectionTestUtils.setField(target, "arca", arca);
        try {
            mvc.perform(pos.en(post("/api/pos/caja/abrir")).content("{\"montoInicial\":0}"));
            String agua = articulo(pos, "7790895000997");
            // ARCA falla: la venta queda igual, con el error, y se reintenta.
            JsonNode v = json(mvc.perform(pos.en(post("/api/pos/ventas")).content("{\"items\":[{\"articuloId\":\"" + agua
                    + "\",\"cantidad\":1}],\"medioPago\":\"QR\"}")).andExpect(status().isCreated()));
            assertThat(v.get("factura").get("error").asText()).isEqualTo("ARCA no responde");
            String id = v.get("id").asText();
            mvc.perform(pos.en(post("/api/pos/ventas/" + id + "/reintentar-factura")))
                    .andExpect(jsonPath("$.factura.cae").value("75123456789012"))
                    .andExpect(jsonPath("$.factura.numero").value(7));
            mvc.perform(pos.en(post("/api/pos/ventas/" + id + "/reintentar-factura"))).andExpect(status().isBadRequest());
            mvc.perform(pos.en(post("/api/pos/ventas/" + id + "/anular")).content("{\"motivo\":\"error de carga\"}"))
                    .andExpect(jsonPath("$.estado").value("ANULADA"));
            Mockito.verify(arca).emitirNotaCredito(eq(tenantId), eq("FACTURA_C"), eq(3), eq(7L), isNull(), any(BigDecimal.class));
        } finally {
            ReflectionTestUtils.setField(target, "arca", original);
        }
    }

    @Test
    void laBajaDeLaTiendaSeLlevaTodoLoDelPos() throws Exception {
        PosPrueba pos = PosPrueba.nuevo(provisioning, mvc, mapper);
        String tenantId = tenants.findBySlug(pos.slug()).orElseThrow().getId();
        mvc.perform(pos.en(post("/api/pos/caja/abrir")).content("{\"montoInicial\":0}"));
        mvc.perform(pos.en(post("/api/pos/ventas")).content("{\"items\":[{\"articuloId\":\"" + articulo(pos, "7790895000997")
                + "\",\"cantidad\":1}],\"medioPago\":\"EFECTIVO\"}")).andExpect(status().isCreated());

        bajas.deleteTenant(tenantId, pos.slug());
        assertThat(tenants.findById(tenantId)).isEmpty();
        assertThat(articulos.findByTenantIdAndArchivadoOrderByNombreAsc(tenantId, false)).isEmpty();
        assertThat(ventas.findByTenantId(tenantId)).isEmpty();
    }
}
