package com.saasweb;

import com.saasweb.core.model.Comprobante;
import com.saasweb.core.model.Sistema;
import com.saasweb.core.service.ArcaInvoiceService.InvoiceResult;
import com.saasweb.core.service.ComprobanteService;
import com.saasweb.core.service.ComprobanteService.Autor;
import com.saasweb.core.service.ComprobanteService.Origen;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Etapa 1f: el núcleo guarda cada comprobante con su origen, aprobado o no. */
@SpringBootTest
class ComprobanteTest {

    @Autowired ComprobanteService comprobantes;

    @Test
    void guardaElIntentoConSuOrigenYSeBuscaPorOrigenYClase() {
        String tenant = "t-" + UUID.randomUUID();
        Origen venta = new Origen(tenant, Sistema.POS, "venta-1");
        Origen otraVenta = new Origen(tenant, Sistema.POS, "venta-2");
        Origen mismoIdOtroSistema = new Origen(tenant, Sistema.RESTAURANTE, "venta-1");

        Comprobante ok = comprobantes.registrar(venta, Comprobante.Clase.FACTURA,
                new InvoiceResult(true, "FACTURA_B", "74123456789012", "20261020", 15L, 3, "https://qr", null),
                new BigDecimal("1500.00"), null, null, new Autor("11111111", "Ruth B"));
        Comprobante rechazado = comprobantes.registrar(venta, Comprobante.Clase.NOTA_CREDITO,
                new InvoiceResult(false, "NC_B", null, null, null, 3, null, "Comprobante rechazado"),
                new BigDecimal("200.00"), "  devolución  ", null, null);
        comprobantes.registrar(otraVenta, Comprobante.Clase.FACTURA,
                new InvoiceResult(true, "FACTURA_B", "1", "20261020", 16L, 3, null, null), BigDecimal.TEN, null, null, null);
        comprobantes.registrar(mismoIdOtroSistema, Comprobante.Clase.FACTURA,
                new InvoiceResult(true, "FACTURA_C", "2", "20261020", 1L, 1, null, null), BigDecimal.ONE, null, null, null);

        assertThat(ok.aprobado()).isTrue();
        assertThat(ok.getNumero()).isEqualTo(15L);
        assertThat(ok.getCreatedByName()).isEqualTo("Ruth B");
        assertThat(rechazado.aprobado()).isFalse();
        assertThat(rechazado.getError()).isEqualTo("Comprobante rechazado");
        assertThat(rechazado.getCae()).isNull();
        assertThat(rechazado.getMotivo()).isEqualTo("devolución");

        assertThat(comprobantes.deOrigen(venta, Comprobante.Clase.FACTURA)).extracting(Comprobante::getId)
                .containsExactly(ok.getId());
        assertThat(comprobantes.deOrigen(venta, Comprobante.Clase.NOTA_CREDITO)).extracting(Comprobante::getId)
                .containsExactly(rechazado.getId());
    }
}
