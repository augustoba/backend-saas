package com.saasweb.restaurante.service;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.OrdenDtos.AbrirMesaRequest;
import com.saasweb.restaurante.dto.OrdenDtos.AbrirMostradorRequest;
import com.saasweb.restaurante.dto.OrdenDtos.AbrirOnlineRequest;
import com.saasweb.restaurante.dto.OrdenDtos.AgregarItemRequest;
import com.saasweb.restaurante.dto.OrdenDtos.ItemCarritoRequest;
import com.saasweb.restaurante.dto.OrdenDtos.EditarItemRequest;
import com.saasweb.restaurante.dto.OrdenDtos.RegistrarPagoRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrdenService {

    private final OrdenRepository ordenRepository;
    private final OrdenItemRepository ordenItemRepository;
    private final PagoRepository pagoRepository;
    private final MesaRepository mesaRepository;
    private final ProductoRepository productoRepository;
    private final MedioPagoRepository medioPagoRepository;
    private final ClienteRepository clienteRepository;
    private final ArqueoRepository arqueoRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    private final SucursalRepository sucursalRepository;
    private final ZonaEnvioRepository zonaEnvioRepository;

    public OrdenService(OrdenRepository ordenRepository, OrdenItemRepository ordenItemRepository,
                         PagoRepository pagoRepository, MesaRepository mesaRepository,
                         ProductoRepository productoRepository, MedioPagoRepository medioPagoRepository,
                         ClienteRepository clienteRepository, ArqueoRepository arqueoRepository,
                         MovimientoCajaRepository movimientoCajaRepository, SucursalRepository sucursalRepository,
                         ZonaEnvioRepository zonaEnvioRepository) {
        this.ordenRepository = ordenRepository;
        this.ordenItemRepository = ordenItemRepository;
        this.pagoRepository = pagoRepository;
        this.mesaRepository = mesaRepository;
        this.productoRepository = productoRepository;
        this.medioPagoRepository = medioPagoRepository;
        this.clienteRepository = clienteRepository;
        this.arqueoRepository = arqueoRepository;
        this.movimientoCajaRepository = movimientoCajaRepository;
        this.sucursalRepository = sucursalRepository;
        this.zonaEnvioRepository = zonaEnvioRepository;
    }

    @Transactional
    public Orden abrirMesa(Usuario usuario, AbrirMesaRequest request) {
        Mesa mesa = mesaRepository.findByIdAndTenantId(request.mesaId(), tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada"));

        ordenRepository.findAbiertaPorMesa(mesa.getId()).ifPresent(o -> {
            throw new BadRequestException("Esa mesa ya tiene una orden abierta");
        });

        Orden orden = new Orden();
        orden.setTenantId(tenantId());
        orden.setSucursalId(sucursalPrincipal());
        orden.setArqueo(arqueoParaAbrir(request.arqueoId()));
        orden.setMesa(mesa);
        orden.setUsuario(usuario);
        orden.setTipo(Orden.Tipo.MESA);
        orden.setComensales(request.comensales());
        orden.setTokenSesion(java.util.UUID.randomUUID().toString());
        orden = ordenRepository.save(orden);

        mesa.setEstado(Mesa.Estado.OCUPADA);
        mesaRepository.save(mesa);
        return orden;
    }

    public Orden abrirMostrador(Usuario usuario, AbrirMostradorRequest request) {
        Orden orden = new Orden();
        orden.setTenantId(tenantId());
        orden.setSucursalId(sucursalPrincipal());
        orden.setArqueo(arqueoParaAbrir(request.arqueoId()));
        orden.setUsuario(usuario);
        orden.setTipo(Orden.Tipo.MOSTRADOR);
        if (request.clienteId() != null) {
            orden.setCliente(clienteRepository.findByIdAndTenantId(request.clienteId(), tenantId()).orElse(null));
        }
        return ordenRepository.save(orden);
    }

    public List<Orden> mostradorAbiertas() {
        return ordenRepository.findMostradorAbiertas(tenantId());
    }

    /**
     * Pedido creado públicamente (carta online + WhatsApp), sin staff detrás
     * todavía — {@code usuario} queda null hasta que alguien lo acepte
     * (confirmarItems) o lo rechace (cancelar), reusando el mismo ciclo de
     * vida que una orden de mesa/mostrador.
     */
    @Transactional
    public Orden abrirOnline(AbrirOnlineRequest request) {
        Orden orden = new Orden();
        orden.setTenantId(tenantId());
        orden.setSucursalId(sucursalPrincipal());
        orden.setArqueo(arqueoAbierto());
        orden.setTipo(request.tipo());
        orden.setNombreClienteOnline(request.nombreCliente());
        orden.setTelefonoClienteOnline(request.telefonoCliente());
        orden.setDireccionEntrega(request.direccionEntrega());

        StringBuilder comentario = new StringBuilder();
        if (request.metodoPago() != null && !request.metodoPago().isBlank()) {
            comentario.append("Método de pago: ").append(request.metodoPago());
        }
        if (request.tipo() == Orden.Tipo.DELIVERY && request.zonaEnvioId() != null) {
            ZonaEnvio zona = zonaEnvioRepository.findByIdAndTenantId(request.zonaEnvioId(), tenantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zona de envío no encontrada"));
            orden.setCostoEnvio(zona.getCosto());
            if (comentario.length() > 0) {
                comentario.append(" | ");
            }
            comentario.append("Zona de envío: ").append(zona.getNombre()).append(" ($").append(zona.getCosto()).append(")");
        }
        if (comentario.length() > 0) {
            orden.setComentario(comentario.toString());
        }
        orden = ordenRepository.save(orden);

        for (ItemCarritoRequest item : request.items()) {
            agregarItem(orden.getId(), new AgregarItemRequest(item.productoId(), item.cantidad(), item.notas()));
        }
        return obtener(orden.getId());
    }

    public List<Orden> pedidosOnlinePendientes() {
        return ordenRepository.findByTenantIdOrderByFechaCreacionDesc(tenantId()).stream()
                .filter(o -> o.getTipo() == Orden.Tipo.DELIVERY || o.getTipo() == Orden.Tipo.LLEVAR)
                .filter(o -> o.getNombreClienteOnline() != null)
                .filter(o -> o.getEstado() == Orden.Estado.EN_CURSO)
                .toList();
    }

    /**
     * Comandas aceptadas (CONFIRMADO) que todavía no se entregaron — para la
     * vista de "Cocina" (Fase 18): TODAS las órdenes confirmadas, sea por un
     * mozo (Mesa/Mostrador) o aceptadas como pedido online, no sólo las
     * online. Separada del KDS general (`core/kds/`) porque acá se sigue la
     * comanda completa (Tomado/Finalizado/Entregado), no ítem por ítem.
     */
    public List<Orden> comandasEnCocina() {
        return ordenRepository.findByTenantIdOrderByFechaCreacionDesc(tenantId()).stream()
                .filter(o -> o.getEstado() == Orden.Estado.CONFIRMADO)
                .filter(o -> o.getEstadoPreparacion() != Orden.EstadoPreparacion.ENTREGADO)
                .sorted(Comparator.comparing(Orden::getFechaConfirmacion, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    @Transactional
    public Orden marcarTomado(Long ordenId) {
        Orden orden = comandaParaCocina(ordenId);
        if (orden.getEstadoPreparacion() != Orden.EstadoPreparacion.SIN_TOMAR) {
            throw new BadRequestException("Esa comanda ya fue tomada");
        }
        orden.setEstadoPreparacion(Orden.EstadoPreparacion.TOMADO);
        orden.setFechaTomado(Instant.now());
        return ordenRepository.save(orden);
    }

    @Transactional
    public Orden marcarListo(Long ordenId) {
        Orden orden = comandaParaCocina(ordenId);
        if (orden.getEstadoPreparacion() != Orden.EstadoPreparacion.TOMADO) {
            throw new BadRequestException("Esa comanda todavía no fue tomada");
        }
        orden.setEstadoPreparacion(Orden.EstadoPreparacion.LISTO);
        return ordenRepository.save(orden);
    }

    @Transactional
    public Orden marcarEntregado(Long ordenId) {
        Orden orden = comandaParaCocina(ordenId);
        if (orden.getEstadoPreparacion() != Orden.EstadoPreparacion.LISTO) {
            throw new BadRequestException("Esa comanda todavía no está lista");
        }
        orden.setEstadoPreparacion(Orden.EstadoPreparacion.ENTREGADO);
        return ordenRepository.save(orden);
    }

    private Orden comandaParaCocina(Long ordenId) {
        Orden orden = obtener(ordenId);
        if (orden.getEstado() != Orden.Estado.CONFIRMADO) {
            throw new BadRequestException("Esa orden todavía no fue confirmada");
        }
        return orden;
    }

    public Orden obtenerPorMesa(Long mesaId) {
        return ordenRepository.findAbiertaPorMesa(mesaId)
                .orElseThrow(() -> new ResourceNotFoundException("Esa mesa no tiene una orden abierta"));
    }

    public Orden obtener(Long id) {
        return ordenRepository.findByIdAndTenantId(id, tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));
    }

    public List<OrdenItem> items(Long ordenId) {
        return ordenItemRepository.findByOrdenIdOrderByIdAsc(ordenId);
    }

    public List<Pago> pagos(Long ordenId) {
        return pagoRepository.findByOrdenIdOrderByIdAsc(ordenId);
    }

    @Transactional
    public Orden agregarItem(Long ordenId, AgregarItemRequest request) {
        Orden orden = obtener(ordenId);
        assertAbierta(orden);

        Producto producto = productoRepository.findByIdAndTenantId(request.productoId(), tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        OrdenItem item = new OrdenItem();
        item.setOrden(orden);
        item.setProducto(producto);
        item.setCantidad(request.cantidad());
        item.setPrecioUnitario(producto.getPrecio() != null ? producto.getPrecio() : BigDecimal.ZERO);
        item.setSubtotal(item.getPrecioUnitario().multiply(BigDecimal.valueOf(request.cantidad())));
        item.setNotas(request.notas());
        item.setPaso(orden.getPasoActual());
        ordenItemRepository.save(item);

        recalcularTotales(orden);
        return orden;
    }

    @Transactional
    public Orden editarItem(Long ordenId, Long itemId, EditarItemRequest request) {
        Orden orden = obtener(ordenId);
        assertAbierta(orden);
        OrdenItem item = ordenItemRepository.findByIdAndOrdenId(itemId, ordenId)
                .orElseThrow(() -> new ResourceNotFoundException("Ítem no encontrado"));

        item.setCantidad(request.cantidad());
        item.setNotas(request.notas());
        item.setSubtotal(item.getPrecioUnitario().multiply(BigDecimal.valueOf(request.cantidad())));
        ordenItemRepository.save(item);

        recalcularTotales(orden);
        return orden;
    }

    @Transactional
    public Orden quitarItem(Long ordenId, Long itemId) {
        Orden orden = obtener(ordenId);
        assertAbierta(orden);
        OrdenItem item = ordenItemRepository.findByIdAndOrdenId(itemId, ordenId)
                .orElseThrow(() -> new ResourceNotFoundException("Ítem no encontrado"));
        ordenItemRepository.delete(item);

        recalcularTotales(orden);
        return orden;
    }

    @Transactional
    public Orden aplicarDescuento(Long ordenId, BigDecimal monto) {
        Orden orden = obtener(ordenId);
        assertAbierta(orden);
        if (monto.compareTo(orden.getSubtotal()) > 0) {
            throw new BadRequestException("El descuento no puede superar el subtotal");
        }
        orden.setDescuentoMonto(monto);
        recalcularTotales(orden);
        return orden;
    }

    @Transactional
    public Orden confirmarItems(Long ordenId) {
        Orden orden = obtener(ordenId);
        assertAbierta(orden);

        List<OrdenItem> pendientes = ordenItemRepository.findByOrdenIdOrderByIdAsc(ordenId).stream()
                .filter(i -> i.getEstado() == OrdenItem.Estado.PENDIENTE && i.getPaso() == orden.getPasoActual())
                .toList();
        if (pendientes.isEmpty()) {
            throw new BadRequestException("No hay ítems pendientes para confirmar");
        }
        pendientes.forEach(i -> i.setEstado(OrdenItem.Estado.EN_PREPARACION));
        ordenItemRepository.saveAll(pendientes);

        orden.setEstado(Orden.Estado.CONFIRMADO);
        orden.setFechaConfirmacion(Instant.now());
        return ordenRepository.save(orden);
    }

    /**
     * "Pasos" (envío por tandas a cocina, {@code Preferencias.pasosPorDefecto}):
     * avanza al siguiente paso para que los ítems que se agreguen de ahora en
     * más queden agrupados aparte y no se envíen a cocina hasta su propio
     * "Confirmar". Exige que el paso actual ya esté confirmado (sin
     * PENDIENTE) para no dejar ítems del paso viejo sin enviar por error.
     */
    @Transactional
    public Orden siguientePaso(Long ordenId) {
        Orden orden = obtener(ordenId);
        assertAbierta(orden);

        boolean quedanPendientesDelPasoActual = ordenItemRepository.findByOrdenIdOrderByIdAsc(ordenId).stream()
                .anyMatch(i -> i.getEstado() == OrdenItem.Estado.PENDIENTE && i.getPaso() == orden.getPasoActual());
        if (quedanPendientesDelPasoActual) {
            throw new BadRequestException("Confirmá el paso actual antes de iniciar uno nuevo");
        }

        orden.setPasoActual(orden.getPasoActual() + 1);
        return ordenRepository.save(orden);
    }

    @Transactional
    public Orden registrarPago(Usuario usuario, Long ordenId, RegistrarPagoRequest request) {
        Orden orden = obtener(ordenId);
        assertAbierta(orden);

        MedioPago medioPago = medioPagoRepository.findByIdAndTenantId(request.medioPagoId(), tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Medio de pago no encontrado"));

        Pago pago = new Pago();
        pago.setOrden(orden);
        pago.setMedioPago(medioPago);
        pago.setMonto(request.monto());
        pago.setPropina(request.propina() != null ? request.propina() : BigDecimal.ZERO);
        pago.setReferencia(request.referencia());
        pago.setUsuarioId(usuario.getId());
        pagoRepository.save(pago);

        orden.setPropina(orden.getPropina().add(pago.getPropina()));

        BigDecimal totalPagado = pagoRepository.findByOrdenIdOrderByIdAsc(ordenId).stream()
                .map(Pago::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPagado.compareTo(orden.getTotal()) >= 0) {
            orden.setEstado(Orden.Estado.PAGADO);
            orden.setFechaPago(Instant.now());
            if (orden.getMesa() != null) {
                Mesa mesa = orden.getMesa();
                mesa.setEstado(Mesa.Estado.LIBRE);
                mesaRepository.save(mesa);
            }
            registrarVentaEnCaja(orden, pago);
        }

        return ordenRepository.save(orden);
    }

    @Transactional
    public Orden cancelar(Long ordenId) {
        Orden orden = obtener(ordenId);
        assertAbierta(orden);
        orden.setEstado(Orden.Estado.CANCELADO);
        if (orden.getMesa() != null) {
            Mesa mesa = orden.getMesa();
            mesa.setEstado(Mesa.Estado.LIBRE);
            mesaRepository.save(mesa);
        }
        return ordenRepository.save(orden);
    }

    private void registrarVentaEnCaja(Orden orden, Pago pago) {
        if (orden.getArqueo() == null) {
            return;
        }
        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setArqueo(orden.getArqueo());
        movimiento.setTipo(pago.getMedioPago().isEsEfectivo() ? MovimientoCaja.Tipo.VENTA : MovimientoCaja.Tipo.VENTA);
        movimiento.setMonto(pago.getMonto());
        movimiento.setDescripcion("Venta orden #" + orden.getId());
        movimiento.setMedioPagoId(pago.getMedioPago().getId());
        movimiento.setUsuarioId(pago.getUsuarioId());
        movimientoCajaRepository.save(movimiento);
    }

    private void recalcularTotales(Orden orden) {
        BigDecimal subtotal = ordenItemRepository.findByOrdenIdOrderByIdAsc(orden.getId()).stream()
                .map(OrdenItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        orden.setSubtotal(subtotal);
        orden.setTotal(subtotal.subtract(orden.getDescuentoMonto()).add(orden.getCostoEnvio()));
        ordenRepository.save(orden);
    }

    private void assertAbierta(Orden orden) {
        if (orden.getEstado() == Orden.Estado.PAGADO || orden.getEstado() == Orden.Estado.CANCELADO) {
            throw new BadRequestException("La orden ya está cerrada");
        }
    }

    private Arqueo arqueoAbierto() {
        return arqueoRepository.findAbiertosPorTenant(tenantId()).stream().findFirst().orElse(null);
    }

    /**
     * Si el front manda una caja activa elegida por el usuario, se usa esa
     * (siempre que siga abierta); si no, se cae al primer arqueo abierto del
     * tenant, mismo criterio que antes de tener selector.
     */
    private Arqueo arqueoParaAbrir(Long arqueoId) {
        if (arqueoId == null) {
            return arqueoAbierto();
        }
        return arqueoRepository.findByIdAndTenantId(arqueoId, tenantId())
                .filter(a -> a.getEstado() == Arqueo.Estado.ABIERTO)
                .orElseGet(this::arqueoAbierto);
    }

    private String tenantId() {
        return TenantContext.getTenantId();
    }

    private Long sucursalPrincipal() {
        return sucursalRepository.findByTenantIdAndActivoTrue(tenantId()).stream()
                .findFirst().orElseThrow(() -> new ResourceNotFoundException("No hay sucursal activa")).getId();
    }
}
