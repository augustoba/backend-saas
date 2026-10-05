package com.saasweb.restaurante.service;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.CajaDtos.AbrirArqueoRequest;
import com.saasweb.restaurante.dto.CajaDtos.ArqueoResponse;
import com.saasweb.restaurante.dto.CajaDtos.CajaRequest;
import com.saasweb.restaurante.dto.CajaDtos.CerrarArqueoRequest;
import com.saasweb.restaurante.dto.CajaDtos.MovimientoRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CajaService {

    private final CajaRepository cajaRepository;
    private final ArqueoRepository arqueoRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    private final com.saasweb.core.repository.AdminUserRepository usuarioRepository;
    private final SucursalRepository sucursalRepository;

    public CajaService(CajaRepository cajaRepository, ArqueoRepository arqueoRepository,
                        MovimientoCajaRepository movimientoCajaRepository, com.saasweb.core.repository.AdminUserRepository usuarioRepository,
                        SucursalRepository sucursalRepository) {
        this.cajaRepository = cajaRepository;
        this.arqueoRepository = arqueoRepository;
        this.movimientoCajaRepository = movimientoCajaRepository;
        this.usuarioRepository = usuarioRepository;
        this.sucursalRepository = sucursalRepository;
    }

    public List<Caja> listarCajas() {
        return cajaRepository.findByTenantIdOrderByNombreAsc(tenantId());
    }

    public Caja crearCaja(CajaRequest request) {
        Caja caja = new Caja();
        caja.setTenantId(tenantId());
        caja.setSucursalId(sucursalPrincipal());
        caja.setNombre(request.nombre());
        caja.setActivo(request.activa());
        return cajaRepository.save(caja);
    }

    public List<ArqueoResponse> arqueosAbiertos() {
        return arqueoRepository.findAbiertosPorTenant(tenantId()).stream().map(this::toResponse).toList();
    }

    public List<ArqueoResponse> historial() {
        return arqueoRepository.findHistorialPorTenant(tenantId()).stream().map(this::toResponse).toList();
    }

    public Arqueo abrir(Usuario usuario, AbrirArqueoRequest request) {
        Caja caja = cajaRepository.findByIdAndTenantId(request.cajaId(), tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Caja no encontrada"));

        if (arqueoRepository.findByCajaIdAndEstado(caja.getId(), Arqueo.Estado.ABIERTO).isPresent()) {
            throw new BadRequestException("Esa caja ya tiene un arqueo abierto");
        }

        Arqueo arqueo = new Arqueo();
        arqueo.setCaja(caja);
        arqueo.setUsuarioAperturaId(usuario.getId());
        arqueo.setMontoApertura(request.montoApertura());
        return arqueoRepository.save(arqueo);
    }

    public Arqueo cerrar(Usuario usuario, Long arqueoId, CerrarArqueoRequest request) {
        Arqueo arqueo = arqueoRepository.findByIdAndTenantId(arqueoId, tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Arqueo no encontrado"));
        if (arqueo.getEstado() != Arqueo.Estado.ABIERTO) {
            throw new BadRequestException("El arqueo ya está cerrado");
        }

        BigDecimal sistema = efectivoEnCaja(arqueo);
        arqueo.setMontoCierreSistema(sistema);
        arqueo.setMontoCierreDeclarado(request.montoCierreDeclarado());
        arqueo.setDiferencia(request.montoCierreDeclarado().subtract(sistema));
        arqueo.setObservaciones(request.observaciones());
        arqueo.setUsuarioCierreId(usuario.getId());
        arqueo.setFechaCierre(Instant.now());
        arqueo.setEstado(Arqueo.Estado.CERRADO);
        return arqueoRepository.save(arqueo);
    }

    public List<MovimientoCaja> listarMovimientos() {
        return movimientoCajaRepository.findByTenantIdOrderByFechaDesc(tenantId());
    }

    public MovimientoCaja crearMovimiento(Usuario usuario, MovimientoRequest request) {
        Arqueo arqueo = arqueoRepository.findByIdAndTenantId(request.arqueoId(), tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Arqueo no encontrado"));
        if (arqueo.getEstado() != Arqueo.Estado.ABIERTO) {
            throw new BadRequestException("El arqueo está cerrado");
        }

        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setArqueo(arqueo);
        movimiento.setTipo(request.tipo());
        movimiento.setMonto(request.monto());
        movimiento.setDescripcion(request.descripcion());
        movimiento.setMedioPagoId(request.medioPagoId());
        movimiento.setUsuarioId(usuario.getId());
        return movimientoCajaRepository.save(movimiento);
    }

    private BigDecimal efectivoEnCaja(Arqueo arqueo) {
        BigDecimal total = arqueo.getMontoApertura();
        for (MovimientoCaja m : movimientoCajaRepository.findByArqueoIdOrderByFechaDesc(arqueo.getId())) {
            switch (m.getTipo()) {
                case INGRESO, VENTA -> total = total.add(m.getMonto());
                case EGRESO, RETIRO, AJUSTE, PAGO_GASTO -> total = total.subtract(m.getMonto());
            }
        }
        return total;
    }

    private ArqueoResponse toResponse(Arqueo a) {
        String nombreUsuario = usuarioRepository.findByIdAndTenantId(a.getUsuarioAperturaId(), tenantId())
                .map(u -> (u.getNombre() + " " + (u.getApellido() != null ? u.getApellido() : "")).trim())
                .orElse("—");
        return new ArqueoResponse(a.getId(), a.getCaja().getNombre(), nombreUsuario, a.getFechaApertura(),
                a.getFechaCierre(), a.getMontoApertura(), a.getMontoCierreDeclarado(), a.getMontoCierreSistema(),
                a.getDiferencia(), a.getEstado(), a.getTotalVentas(), efectivoEnCaja(a));
    }

    private String tenantId() {
        return TenantContext.getTenantId();
    }

    private Long sucursalPrincipal() {
        return sucursalRepository.findByTenantIdAndActivoTrue(tenantId()).stream()
                .findFirst().orElseThrow(() -> new ResourceNotFoundException("No hay sucursal activa")).getId();
    }
}
