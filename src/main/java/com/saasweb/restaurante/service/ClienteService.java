package com.saasweb.restaurante.service;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.ClienteDtos.ClienteKpis;
import com.saasweb.restaurante.dto.ClienteDtos.ClienteRequest;
import com.saasweb.restaurante.dto.ClienteDtos.MovimientoCuentaCorrienteRequest;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final CuentaCorrienteMovimientoRepository movimientoRepository;

    public ClienteService(ClienteRepository clienteRepository, CuentaCorrienteMovimientoRepository movimientoRepository) {
        this.clienteRepository = clienteRepository;
        this.movimientoRepository = movimientoRepository;
    }

    public Page<Cliente> buscar(String q, Pageable pageable) {
        String normalizado = (q == null || q.isBlank()) ? null : q.toLowerCase();
        return clienteRepository.buscar(tenantId(), normalizado, pageable);
    }

    public ClienteKpis kpis() {
        String t = tenantId();
        return new ClienteKpis(
                clienteRepository.countByTenantId(t),
                clienteRepository.countByTenantIdAndTieneCuentaCorrienteTrue(t),
                clienteRepository.countConDescuento(t),
                clienteRepository.countByTenantIdAndActivoTrue(t));
    }

    public Cliente crear(ClienteRequest request) {
        Cliente cliente = new Cliente();
        cliente.setTenantId(tenantId());
        aplicar(cliente, request);
        return clienteRepository.save(cliente);
    }

    public Cliente actualizar(Long id, ClienteRequest request) {
        Cliente cliente = obtener(id);
        aplicar(cliente, request);
        return clienteRepository.save(cliente);
    }

    public void archivar(Long id) {
        Cliente cliente = obtener(id);
        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }

    public Cliente habilitarCuentaCorriente(Long id, boolean habilitada) {
        Cliente cliente = obtener(id);
        cliente.setTieneCuentaCorriente(habilitada);
        return clienteRepository.save(cliente);
    }

    public List<CuentaCorrienteMovimiento> listarMovimientos(Long clienteId) {
        obtener(clienteId);
        return movimientoRepository.findByClienteIdOrderByFechaDescIdDesc(clienteId);
    }

    public CuentaCorrienteMovimiento registrarMovimiento(Long clienteId, MovimientoCuentaCorrienteRequest request) {
        Cliente cliente = obtener(clienteId);
        if (!cliente.isTieneCuentaCorriente()) {
            throw new BadRequestException("El cliente no tiene cuenta corriente habilitada");
        }

        BigDecimal saldoActual = cliente.getSaldoCuentaCorriente();
        BigDecimal nuevoSaldo = request.tipo() == CuentaCorrienteMovimiento.Tipo.CARGO
                ? saldoActual.add(request.monto())
                : saldoActual.subtract(request.monto());
        cliente.setSaldoCuentaCorriente(nuevoSaldo);
        clienteRepository.save(cliente);

        CuentaCorrienteMovimiento movimiento = new CuentaCorrienteMovimiento();
        movimiento.setTenantId(tenantId());
        movimiento.setCliente(cliente);
        movimiento.setTipo(request.tipo());
        movimiento.setMonto(request.monto());
        movimiento.setDescripcion(request.descripcion());
        movimiento.setSaldoResultante(nuevoSaldo);
        return movimientoRepository.save(movimiento);
    }

    private Cliente obtener(Long id) {
        return clienteRepository.findByIdAndTenantId(id, tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
    }

    private void aplicar(Cliente cliente, ClienteRequest request) {
        cliente.setNombre(request.nombre());
        cliente.setEmail(request.email());
        cliente.setTelefono(request.telefono());
        cliente.setCuit(request.cuit());
        cliente.setInstagram(request.instagram());
        cliente.setDireccion(request.direccion());
        cliente.setNotas(request.notas());
        cliente.setTieneCuentaCorriente(request.tieneCuentaCorriente());
        cliente.setLimiteCredito(request.limiteCredito());
        cliente.setDescuentoPorcentaje(request.descuentoPorcentaje());
    }

    private String tenantId() {
        return TenantContext.getTenantId();
    }
}
