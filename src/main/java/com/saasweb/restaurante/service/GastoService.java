package com.saasweb.restaurante.service;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.GastoDtos.CategoriaGastoRequest;
import com.saasweb.restaurante.dto.GastoDtos.ClasificacionRequest;
import com.saasweb.restaurante.dto.GastoDtos.GastoKpis;
import com.saasweb.restaurante.dto.GastoDtos.GastoRequest;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GastoService {

    private final ClasificacionGastoRepository clasificacionRepository;
    private final CategoriaGastoRepository categoriaRepository;
    private final GastoRepository gastoRepository;
    private final ProveedorRepository proveedorRepository;
    private final SucursalRepository sucursalRepository;

    public GastoService(ClasificacionGastoRepository clasificacionRepository, CategoriaGastoRepository categoriaRepository,
                         GastoRepository gastoRepository, ProveedorRepository proveedorRepository,
                         SucursalRepository sucursalRepository) {
        this.clasificacionRepository = clasificacionRepository;
        this.categoriaRepository = categoriaRepository;
        this.gastoRepository = gastoRepository;
        this.proveedorRepository = proveedorRepository;
        this.sucursalRepository = sucursalRepository;
    }

    // Clasificaciones
    public List<ClasificacionGasto> listarClasificaciones() {
        return clasificacionRepository.findByTenantIdOrderByNombreAsc(tenantId());
    }

    public ClasificacionGasto crearClasificacion(ClasificacionRequest request) {
        ClasificacionGasto c = new ClasificacionGasto();
        c.setTenantId(tenantId());
        c.setNombre(request.nombre());
        return clasificacionRepository.save(c);
    }

    public void archivarClasificacion(Long id) {
        ClasificacionGasto c = clasificacionRepository.findByIdAndTenantId(id, tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Clasificación no encontrada"));
        c.setActivo(false);
        clasificacionRepository.save(c);
    }

    // Categorías
    public List<CategoriaGasto> listarCategorias() {
        return categoriaRepository.findByTenantIdOrderByNombreAsc(tenantId());
    }

    public CategoriaGasto crearCategoria(CategoriaGastoRequest request) {
        ClasificacionGasto clasificacion = clasificacionRepository.findByIdAndTenantId(request.clasificacionId(), tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Clasificación no encontrada"));
        CategoriaGasto categoria = new CategoriaGasto();
        categoria.setTenantId(tenantId());
        categoria.setClasificacion(clasificacion);
        categoria.setNombre(request.nombre());
        return categoriaRepository.save(categoria);
    }

    // Gastos
    public List<Gasto> listar() {
        return gastoRepository.findByTenantIdOrderByFechaDesc(tenantId());
    }

    public GastoKpis kpis() {
        List<Gasto> gastos = listar();
        BigDecimal totalAPagar = gastos.stream()
                .filter(g -> g.getEstado() == Gasto.Estado.PENDIENTE || g.getEstado() == Gasto.Estado.VENCIDO)
                .map(Gasto::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPagado = gastos.stream()
                .filter(g -> g.getEstado() == Gasto.Estado.PAGADO)
                .map(Gasto::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new GastoKpis(gastoRepository.countAVencer(tenantId()), gastoRepository.countVencidos(tenantId()),
                totalAPagar, totalPagado);
    }

    public Gasto crear(GastoRequest request) {
        Gasto gasto = new Gasto();
        gasto.setTenantId(tenantId());
        gasto.setSucursalId(sucursalPrincipal());
        aplicar(gasto, request);
        return gastoRepository.save(gasto);
    }

    public Gasto marcarPagado(Long id) {
        Gasto gasto = obtener(id);
        gasto.setEstado(Gasto.Estado.PAGADO);
        gasto.setFechaPago(java.time.LocalDate.now());
        return gastoRepository.save(gasto);
    }

    public List<Gasto> listarSinAsignar() {
        return gastoRepository.findByTenantIdAndProveedorIsNullOrderByFechaDesc(tenantId());
    }

    public Gasto asignarProveedor(Long id, Long proveedorId) {
        Gasto gasto = obtener(id);
        gasto.setProveedor(proveedorRepository.findByIdAndTenantId(proveedorId, tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado")));
        return gastoRepository.save(gasto);
    }

    private Gasto obtener(Long id) {
        return gastoRepository.findByIdAndTenantId(id, tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Gasto no encontrado"));
    }

    private void aplicar(Gasto gasto, GastoRequest request) {
        if (request.proveedorId() != null) {
            gasto.setProveedor(proveedorRepository.findByIdAndTenantId(request.proveedorId(), tenantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado")));
        }
        CategoriaGasto categoria = categoriaRepository.findByIdAndTenantId(request.categoriaGastoId(), tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría de gasto no encontrada"));
        gasto.setCategoriaGasto(categoria);
        gasto.setFecha(request.fecha());
        gasto.setFechaVencimiento(request.fechaVencimiento());
        gasto.setMonto(request.monto());
        gasto.setDescripcion(request.descripcion());
        gasto.setNumeroComprobante(request.numeroComprobante());
    }

    private String tenantId() {
        return TenantContext.getTenantId();
    }

    private Long sucursalPrincipal() {
        return sucursalRepository.findByTenantIdAndActivoTrue(tenantId()).stream()
                .findFirst().orElseThrow(() -> new ResourceNotFoundException("No hay sucursal activa")).getId();
    }
}
