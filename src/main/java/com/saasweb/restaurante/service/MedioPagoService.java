package com.saasweb.restaurante.service;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.MedioPagoDtos.MedioPagoRequest;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MedioPagoService {

    private final MedioPagoRepository medioPagoRepository;

    public MedioPagoService(MedioPagoRepository medioPagoRepository) {
        this.medioPagoRepository = medioPagoRepository;
    }

    public List<MedioPago> listar() {
        return medioPagoRepository.findByTenantIdOrderByOrdenAsc(tenantId());
    }

    public MedioPago crear(MedioPagoRequest request) {
        MedioPago medioPago = new MedioPago();
        medioPago.setTenantId(tenantId());
        medioPago.setOrden((int) medioPagoRepository.findByTenantIdOrderByOrdenAsc(tenantId()).stream().count());
        aplicar(medioPago, request);
        return medioPagoRepository.save(medioPago);
    }

    public MedioPago actualizar(Long id, MedioPagoRequest request) {
        MedioPago medioPago = obtener(id);
        aplicar(medioPago, request);
        return medioPagoRepository.save(medioPago);
    }

    public void reordenar(List<Long> ids) {
        int orden = 0;
        for (Long id : ids) {
            MedioPago medioPago = obtener(id);
            medioPago.setOrden(orden++);
            medioPagoRepository.save(medioPago);
        }
    }

    public void archivar(Long id) {
        MedioPago medioPago = obtener(id);
        medioPago.setActivo(false);
        medioPagoRepository.save(medioPago);
    }

    private MedioPago obtener(Long id) {
        return medioPagoRepository.findByIdAndTenantId(id, tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Medio de pago no encontrado"));
    }

    private void aplicar(MedioPago medioPago, MedioPagoRequest request) {
        medioPago.setNombre(request.nombre());
        medioPago.setEsEfectivo(request.esEfectivo());
        medioPago.setEsCuentaCorriente(request.esCuentaCorriente());
        medioPago.setRequiereReferencia(request.requiereReferencia());
    }

    private String tenantId() {
        return TenantContext.getTenantId();
    }
}
