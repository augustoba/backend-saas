package com.saasweb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saasweb.core.common.TenantContext;
import com.saasweb.ecommerce.model.Order;
import com.saasweb.ecommerce.service.OrderService;
import com.saasweb.core.service.TenantService;
import com.saasweb.core.model.CobroMp;
import com.saasweb.core.model.Sistema;
import com.saasweb.core.repository.CobroMpRepository;
import com.saasweb.core.service.CobroService;
import com.saasweb.core.service.MercadoPagoService.PaymentInfo;
import com.saasweb.ecommerce.repository.OrderRepository;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Pago aprobado por Mercado Pago, por el camino real desde la Etapa 2.3b: el
 * cobro vive en el núcleo ({@code CobroService}), que publica
 * {@code CobroActualizado} y el ecommerce confirma el pedido (o deja el aviso
 * de "pago aprobado pero no se pudo confirmar solo"). No llama a la API de
 * Mercado Pago: se aplica directo el pago ya "consultado".
 */
@SpringBootTest
@AutoConfigureMockMvc
class OrderMercadoPagoConfirmTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    OrderService orderService;
    @Autowired
    TenantService tenantService;
    @Autowired
    CobroService cobroService;
    @Autowired
    CobroMpRepository cobros;
    @Autowired
    OrderRepository orders;
    @Autowired
    TransactionTemplate tx;

    /** Lo que importa del pedido después del pago (leído dentro de una transacción: el cobro es lazy). */
    record Resultado(String paymentStatus, String mpPaymentId, String status, String paymentIssueNote) {}

    /** Crea el cobro del pedido y le aplica un pago aprobado, como si viniera del webhook. */
    private Resultado aprobarPago(String orderId, String mpPaymentId) {
        String tenantId = tenantService.resolveCurrentTenantId();
        TenantContext.set(tenantId);
        try {
            String cobroId = tx.execute(s -> {
                CobroMp c = new CobroMp();
                c.setId(UUID.randomUUID().toString());
                c.setTenantId(tenantId);
                c.setOrigenSistema(Sistema.ECOMMERCE);
                c.setOrigenId(orderId);
                CobroMp guardado = cobros.save(c);
                Order o = orders.findByIdAndTenantId(orderId, tenantId).orElseThrow();
                o.setCobro(guardado);
                orders.save(o);
                return guardado.getId();
            });
            tx.executeWithoutResult(s -> cobroService.aplicar(tenantId, new PaymentInfo(mpPaymentId, "approved", cobroId)));
            return tx.execute(s -> {
                Order o = orderService.get(orderId);
                return new Resultado(o.getPaymentStatus().name(), o.getMpPaymentId(), o.getStatus().name(),
                        o.getPaymentIssueNote());
            });
        } finally {
            TenantContext.clear();
        }
    }

    private String token() throws Exception {
        String body = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dni\":\"11111111\",\"password\":\"test-pass\"}"))
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("token").asText();
    }

    private String createProduct(String jwt, int stock) throws Exception {
        String scaleId = mapper.readTree(
                        mvc.perform(get("/api/size-scales")).andReturn().getResponse().getContentAsString())
                .get(0).get("id").asText();
        String create = "{\"name\":\"MP test\",\"description\":\"prueba\",\"price\":1000,"
                + "\"ageRange\":\"1 a 2\",\"images\":[\"https://x/a.jpg\"],\"sizeScaleId\":\"" + scaleId + "\","
                + "\"params\":{},\"sizeStocks\":[{\"size\":\"RN\",\"stock\":" + stock + "}]}";
        return mapper.readTree(mvc.perform(post("/api/admin/products")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON).content(create))
                .andReturn().getResponse().getContentAsString()).get("id").asText();
    }

    private String createOrder(String productId, int quantity) throws Exception {
        return mapper.readTree(mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerName\":\"X\",\"items\":[{\"productId\":\"" + productId
                                + "\",\"size\":\"RN\",\"quantity\":" + quantity + "}]}"))
                .andReturn().getResponse().getContentAsString()).get("id").asText();
    }

    @Test
    void keepsPaymentApprovedAndLeavesAVisibleNoteWhenStockIsNotEnough() throws Exception {
        String jwt = token();
        String productId = createProduct(jwt, 1);
        String orderId = createOrder(productId, 3);

        Resultado result = aprobarPago(orderId, "fake-mp-payment-id");

        // el pago quedó marcado como aprobado (el cliente pagó de verdad)...
        assertThat(result.paymentStatus()).isEqualTo("APPROVED");
        assertThat(result.mpPaymentId()).isEqualTo("fake-mp-payment-id");
        // ...pero el pedido NO se confirmó solo, y queda un aviso legible
        assertThat(result.status()).isEqualTo("PENDIENTE");
        assertThat(result.paymentIssueNote()).contains("Mercado Pago aprobó el pago");

        // y el stock no se tocó
        JsonNode product = mapper.readTree(mvc.perform(get("/api/products/" + productId))
                .andReturn().getResponse().getContentAsString());
        assertThat(product.get("sizeStocks").get(0).get("stock").asInt()).isEqualTo(1);
    }

    @Test
    void confirmsNormallyAndLeavesNoNoteWhenStockIsEnough() throws Exception {
        String jwt = token();
        String productId = createProduct(jwt, 5);
        String orderId = createOrder(productId, 2);

        Resultado result = aprobarPago(orderId, "fake-mp-payment-id-2");

        assertThat(result.paymentStatus()).isEqualTo("APPROVED");
        assertThat(result.status()).isEqualTo("PROCESADO");
        assertThat(result.paymentIssueNote()).isNull();

        JsonNode product = mapper.readTree(mvc.perform(get("/api/products/" + productId))
                .andReturn().getResponse().getContentAsString());
        assertThat(product.get("sizeStocks").get(0).get("stock").asInt()).isEqualTo(3);
    }
}
