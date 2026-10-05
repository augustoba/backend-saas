package com.saasweb.pos.service;

import com.saasweb.core.service.LimpiezaDeTienda;
import com.saasweb.pos.repository.ArticuloRepository;
import com.saasweb.pos.repository.CajaRepository;
import com.saasweb.pos.repository.CategoriaRepository;
import com.saasweb.pos.repository.GastoRepository;
import com.saasweb.pos.repository.ProveedorRepository;
import com.saasweb.pos.repository.MovimientoCajaRepository;
import com.saasweb.pos.repository.MovimientoStockRepository;
import com.saasweb.pos.repository.VentaRepository;
import org.springframework.stereotype.Component;

/**
 * Borra todo lo que el POS guardó para una tienda (baja definitiva). Las
 * ventas se borran entidad por entidad para que Hibernate se lleve sus líneas.
 */
@Component
public class LimpiezaPos implements LimpiezaDeTienda {

    private final VentaRepository ventas;
    private final MovimientoCajaRepository movimientosCaja;
    private final CajaRepository cajas;
    private final MovimientoStockRepository movimientos;
    private final ArticuloRepository articulos;
    private final CategoriaRepository categorias;
    private final GastoRepository gastos;
    private final ProveedorRepository proveedores;

    public LimpiezaPos(VentaRepository ventas, MovimientoCajaRepository movimientosCaja, CajaRepository cajas,
                       MovimientoStockRepository movimientos, ArticuloRepository articulos,
                       CategoriaRepository categorias, GastoRepository gastos, ProveedorRepository proveedores) {
        this.gastos = gastos;
        this.proveedores = proveedores;
        this.ventas = ventas;
        this.movimientosCaja = movimientosCaja;
        this.cajas = cajas;
        this.movimientos = movimientos;
        this.articulos = articulos;
        this.categorias = categorias;
    }

    @Override
    public void borrarDatosDe(String tenantId) {
        ventas.deleteAll(ventas.findByTenantId(tenantId));
        gastos.deleteByTenantId(tenantId);
        proveedores.deleteByTenantId(tenantId);
        movimientosCaja.deleteByTenantId(tenantId);
        cajas.deleteByTenantId(tenantId);
        movimientos.deleteByTenantId(tenantId);
        articulos.deleteByTenantId(tenantId);
        categorias.deleteByTenantId(tenantId);
    }
}
