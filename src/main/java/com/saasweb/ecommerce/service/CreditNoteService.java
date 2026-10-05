package com.saasweb.ecommerce.service;

import com.saasweb.core.service.ArcaInvoiceService;
import com.saasweb.core.service.ComprobanteService;

import com.saasweb.core.model.Comprobante;
import com.saasweb.core.model.Sistema;

import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;
import com.saasweb.core.repository.AdminUserRepository;
import com.saasweb.core.service.ArcaInvoiceService.InvoiceResult;
import com.saasweb.ecommerce.model.Order;
import com.saasweb.ecommerce.repository.OrderRepository;
import com.saasweb.ecommerce.model.OrderStatus;
import com.saasweb.ecommerce.model.SaleChannel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class CreditNoteService {

    private final ComprobanteService comprobantes;
    private final OrderRepository orderRepo;
    private final ArcaInvoiceService arcaInvoiceService;
    private final AdminUserRepository adminUsers;

    public CreditNoteService(ComprobanteService comprobantes, OrderRepository orderRepo,
                             ArcaInvoiceService arcaInvoiceService, AdminUserRepository adminUsers) {
        this.comprobantes = comprobantes;
        this.orderRepo = orderRepo;
        this.arcaInvoiceService = arcaInvoiceService;
        this.adminUsers = adminUsers;
    }

    @Transactional(readOnly = true)
    public List<Comprobante> findByOrder(String orderId) {
        return comprobantes.deOrigen(origen(orderId), Comprobante.Clase.NOTA_CREDITO);
    }

    /**
     * Emite una nota de crédito contra un pedido ya facturado con ARCA —
     * acción manual del panel (ítem 2), monto y motivo los decide quien la
     * pide. No toca stock ni el estado del pedido; siempre queda un registro
     * (apruebe o no ARCA), para poder ver el historial de intentos.
     */
    public Comprobante emit(String orderId, BigDecimal amount, String reason, String createdByDni) {
        String tenantId = TenantContext.getTenantId();
        Order order = orderRepo.findByIdAndTenantId(orderId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.of("Pedido", orderId));
        if (order.getChannel() != SaleChannel.LOCAL || order.getStatus() != OrderStatus.PROCESADO) {
            throw new BadRequestException("Sólo se puede emitir una NC de una venta presencial ya cobrada.");
        }
        if (order.getInvoiceType() == null || !order.getInvoiceType().startsWith("FACTURA_")
                || order.getInvoiceCae() == null) {
            throw new BadRequestException("Este pedido no tiene una Factura ARCA aprobada.");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new BadRequestException("El monto tiene que ser mayor a 0.");
        }

        InvoiceResult result = arcaInvoiceService.emitirNotaCredito(tenantId, order.getInvoiceType(),
                order.getInvoicePuntoVenta(), order.getInvoiceNumber(), order.getInvoiceBuyerCuit(), amount);

        return comprobantes.registrar(origen(orderId), Comprobante.Clase.NOTA_CREDITO, result, amount, reason,
                order.getInvoiceBuyerCuit(), new ComprobanteService.Autor(createdByDni, nameByDni(createdByDni)));
    }

    /** Las notas de crédito de hoy son de pedidos del ecommerce. */
    private static ComprobanteService.Origen origen(String orderId) {
        return new ComprobanteService.Origen(TenantContext.getTenantId(), Sistema.ECOMMERCE, orderId);
    }

    private String nameByDni(String dni) {
        if (dni == null || dni.isBlank()) return null;
        return adminUsers.findByDniForTenant(dni, TenantContext.getTenantId())
                .map(u -> u.getNombre() + " " + u.getApellido()).orElse(null);
    }
}
