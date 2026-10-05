package com.saasweb.ecommerce.service;

import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;
import com.saasweb.core.model.AdminUser;
import com.saasweb.ecommerce.model.Shift;
import com.saasweb.core.repository.AdminUserRepository;
import com.saasweb.ecommerce.repository.ShiftRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ShiftService {

    private final ShiftRepository repo;
    private final AdminUserRepository users;

    public ShiftService(ShiftRepository repo, AdminUserRepository users) {
        this.repo = repo;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public Shift get(String id) {
        return repo.findByIdAndTenantId(id, TenantContext.getTenantId())
                .orElseThrow(() -> ResourceNotFoundException.of("Turno", id));
    }

    @Transactional(readOnly = true)
    public Optional<Shift> currentOpen(String userDni) {
        return repo.findByTenantIdAndUserDniAndClosedAtIsNull(TenantContext.getTenantId(), userDni);
    }

    @Transactional(readOnly = true)
    public Page<Shift> list(String userDni, Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        return userDni == null || userDni.isBlank()
                ? repo.findByTenantIdOrderByOpenedAtDesc(tenantId, pageable)
                : repo.findByTenantIdAndUserDniOrderByOpenedAtDesc(tenantId, userDni, pageable);
    }

    public Shift open(String userDni) {
        String tenantId = TenantContext.getTenantId();
        if (repo.findByTenantIdAndUserDniAndClosedAtIsNull(tenantId, userDni).isPresent()) {
            throw new BadRequestException("Ya tenés un turno abierto.");
        }
        AdminUser user = users.findByDniForTenant(userDni, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.of("Usuario", userDni));
        Shift shift = new Shift();
        shift.setId(UUID.randomUUID().toString());
        shift.setTenantId(tenantId);
        shift.setUserDni(userDni);
        shift.setUserName(user.getNombre() + " " + user.getApellido());
        shift.setOpenedAt(Instant.now());
        return repo.save(shift);
    }

    /** Lo puede cerrar el dueño del turno, o alguien que puede ver la caja (turnos olvidados). */
    public Shift close(String shiftId, String actingDni) {
        Shift shift = get(shiftId);
        if (shift.getClosedAt() != null) {
            throw new BadRequestException("Ese turno ya está cerrado.");
        }
        if (!shift.getUserDni().equals(actingDni)) {
            AdminUser acting = users.findByDniForTenant(actingDni, TenantContext.getTenantId()).orElse(null);
            boolean canForceClose = acting != null && (acting.isSystemAdmin() || acting.permissions().contains("CASH_REGISTER_VIEW"));
            if (!canForceClose) {
                throw new BadRequestException("Sólo el dueño del turno (o alguien que puede ver la caja) puede cerrarlo.");
            }
        }
        shift.setClosedAt(Instant.now());
        return repo.save(shift);
    }
}
