package com.saasweb.core.service;

import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.model.Tenant;
import com.saasweb.core.repository.AdminUserRepository;
import com.saasweb.core.repository.CobroMpRepository;
import com.saasweb.core.repository.ComprobanteRepository;
import com.saasweb.core.repository.DominioRepository;
import com.saasweb.core.repository.RoleRepository;
import com.saasweb.core.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Borrado permanente de un tenant y TODOS sus datos — irreversible. Pensado
 * para el botón "Eliminar tienda" del superadmin (confirmación en el
 * frontend: hay que tipear el slug exacto antes de habilitar el botón).
 *
 * Primero cada sistema borra lo suyo ({@link LimpiezaDeTienda}); después el
 * núcleo borra sus tablas y la fila del tenant. AdminUser se borra ANTES que
 * Role (la FK es un @ManyToOne sin cascade) y el rol de sistema (superadmin,
 * tenantId null) nunca se toca.
 *
 * Lo que este borrado NO puede hacer: eliminar el logo/carrusel/fotos de
 * Cloudinary — el preset unsigned que usa esta plataforma no tiene permiso
 * para borrar assets (hace falta la API key/secret firmada, que no está
 * configurada). Esos archivos quedan huérfanos en la cuenta de Cloudinary;
 * hay que borrarlos a mano ahí si hace falta liberar espacio.
 */
@Service
public class TenantDeletionService {

    private final TenantRepository tenantRepo;
    private final AdminUserRepository adminUserRepo;
    private final RoleRepository roleRepo;
    private final ComprobanteRepository comprobanteRepo;
    private final CobroMpRepository cobroRepo;
    private final DominioRepository dominioRepo;
    private final ConfiguracionService configuracion;
    private final List<LimpiezaDeTienda> limpiezas;

    public TenantDeletionService(TenantRepository tenantRepo, AdminUserRepository adminUserRepo, RoleRepository roleRepo,
                                 ComprobanteRepository comprobanteRepo, CobroMpRepository cobroRepo, DominioRepository dominioRepo,
                                 ConfiguracionService configuracion, List<LimpiezaDeTienda> limpiezas) {
        this.tenantRepo = tenantRepo;
        this.adminUserRepo = adminUserRepo;
        this.roleRepo = roleRepo;
        this.comprobanteRepo = comprobanteRepo;
        this.cobroRepo = cobroRepo;
        this.dominioRepo = dominioRepo;
        this.configuracion = configuracion;
        this.limpiezas = limpiezas;
    }

    @Transactional
    public void deleteTenant(String tenantId, String confirmSlug) {
        Tenant t = tenantRepo.findById(tenantId)
                .orElseThrow(() -> new BadRequestException("La tienda no existe."));
        if (confirmSlug == null || !confirmSlug.trim().equals(t.getSlug())) {
            throw new BadRequestException("El identificador escrito no coincide con el de la tienda.");
        }

        limpiezas.forEach(l -> l.borrarDatosDe(tenantId));

        comprobanteRepo.deleteAllByTenantId(tenantId);
        cobroRepo.deleteAllByTenantId(tenantId);
        // AdminUser antes que Role (ver javadoc de la clase).
        adminUserRepo.deleteAllByTenantId(tenantId);
        roleRepo.deleteAllByTenantId(tenantId);
        configuracion.borrarDe(tenantId);
        dominioRepo.deleteByTenantId(tenantId);
        tenantRepo.delete(t);
    }
}
