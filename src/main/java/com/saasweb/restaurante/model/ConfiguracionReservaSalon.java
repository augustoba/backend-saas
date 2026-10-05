package com.saasweb.restaurante.model;

import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.restaurante.service.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Capacidad reservable por salón — puede ser menor a la capacidad física (relevamiento §4.10). */
@Entity
@Table(name = "rs_config_reserva_salon")
public class ConfiguracionReservaSalon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "salon_id", nullable = false, unique = true)
    private Long salonId;

    @Column(name = "capacidad_reservable", nullable = false)
    private int capacidadReservable = 0;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSalonId() {
        return salonId;
    }

    public void setSalonId(Long salonId) {
        this.salonId = salonId;
    }

    public int getCapacidadReservable() {
        return capacidadReservable;
    }

    public void setCapacidadReservable(int capacidadReservable) {
        this.capacidadReservable = capacidadReservable;
    }
}
