package com.saasweb.core.service;

import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.config.AppProperties;
import com.saasweb.core.model.CobroMp;
import com.saasweb.core.model.ConfigMp;
import com.saasweb.core.model.EstadoCobro;
import com.saasweb.core.repository.CobroMpRepository;
import com.saasweb.core.service.ComprobanteService.Origen;
import com.saasweb.core.service.MercadoPagoService.PaymentInfo;
import com.saasweb.core.service.MercadoPagoService.PreferenceItem;
import com.saasweb.core.service.MercadoPagoService.PreferenceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Cobros online con Mercado Pago para cualquier sistema: crea la preferencia
 * con la cuenta de la tienda y, cuando llega la notificación, actualiza el
 * cobro y avisa al sistema dueño con {@link CobroActualizado}.
 */
@Service
@Transactional
public class CobroService {

    private static final Logger log = LoggerFactory.getLogger(CobroService.class);

    private final CobroMpRepository repo;
    private final MercadoPagoService mercadoPago;
    private final ConfiguracionService configuracion;
    private final AppProperties props;
    private final ApplicationEventPublisher eventos;

    public CobroService(CobroMpRepository repo, MercadoPagoService mercadoPago, ConfiguracionService configuracion,
                        AppProperties props, ApplicationEventPublisher eventos) {
        this.repo = repo;
        this.mercadoPago = mercadoPago;
        this.configuracion = configuracion;
        this.props = props;
        this.eventos = eventos;
    }

    /**
     * Crea el cobro y su preferencia de Checkout Pro. {@code volverA} es la URL a
     * la que vuelve el cliente; se le agrega {@code pago=aprobado|pendiente|rechazado}.
     */
    public CobroMp crear(Origen origen, List<PreferenceItem> items, String volverA) {
        ConfigMp cfg = configuracion.mp(origen.tenantId());
        String accessToken = cfg.getMpAccessToken();
        if (accessToken == null || accessToken.isBlank()) {
            throw new BadRequestException("Esta tienda todavía no configuró Mercado Pago.");
        }
        CobroMp c = new CobroMp();
        c.setId(UUID.randomUUID().toString());
        c.setTenantId(origen.tenantId());
        c.setOrigenSistema(origen.sistema());
        c.setOrigenId(origen.id());

        String notificationUrl = props.getUrls().getBackend() + "/api/webhooks/mercadopago?tenantId=" + origen.tenantId();
        String sep = volverA.contains("?") ? "&" : "?";
        PreferenceResult pref = mercadoPago.createPreference(accessToken, c.getId(), items, notificationUrl,
                volverA + sep + "pago=aprobado", volverA + sep + "pago=pendiente", volverA + sep + "pago=rechazado");
        c.setPreferenceId(pref.preferenceId());
        c.setCheckoutUrl(pref.initPoint());
        return repo.save(c);
    }

    /**
     * Notificación de Mercado Pago: se consulta el pago REAL en su API (nunca se
     * confía en el cuerpo del webhook) y, si cambió de estado, se publica el
     * evento para el sistema dueño. Sin cobro conocido, se ignora.
     */
    public void procesarNotificacion(String tenantId, String paymentId) {
        String accessToken = configuracion.mp(tenantId).getMpAccessToken();
        if (accessToken == null || accessToken.isBlank()) {
            log.warn("Notificación de Mercado Pago para tenant {} sin Access Token cargado: ignorada.", tenantId);
            return;
        }
        PaymentInfo payment = mercadoPago.getPayment(accessToken, paymentId);
        aplicar(tenantId, payment);
    }

    /**
     * Aplica un pago ya consultado a su cobro. La referencia externa es el id del
     * cobro; para preferencias creadas antes de que existiera el cobro era el id
     * del documento (el pedido), así que se busca también por ahí.
     */
    public void aplicar(String tenantId, PaymentInfo payment) {
        String ref = payment.externalReference();
        if (ref == null || ref.isBlank()) return;
        Optional<CobroMp> cobro = repo.findByIdAndTenantId(ref, tenantId)
                .or(() -> repo.findFirstByTenantIdAndOrigenIdOrderByCreatedAtDesc(tenantId, ref));
        if (cobro.isEmpty()) {
            log.warn("Pago {} de Mercado Pago con referencia {} desconocida (tenant {}).", payment.id(), ref, tenantId);
            return;
        }
        EstadoCobro estado = switch (payment.status()) {
            case "approved" -> EstadoCobro.APPROVED;
            case "rejected", "cancelled" -> EstadoCobro.REJECTED;
            default -> null; // pending / in_process: se espera la próxima notificación
        };
        if (estado == null) return;
        CobroMp c = cobro.get();
        c.setEstado(estado);
        c.setPaymentId(payment.id());
        repo.save(c);
        eventos.publishEvent(new CobroActualizado(tenantId, c.getOrigenSistema(), c.getOrigenId(), estado, payment.id()));
    }
}
