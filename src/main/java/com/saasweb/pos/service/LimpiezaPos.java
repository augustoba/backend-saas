package com.saasweb.pos.service;

import com.saasweb.core.service.LimpiezaDeTienda;
import com.saasweb.pos.repository.ArticuloRepository;
import com.saasweb.pos.repository.CategoriaRepository;
import com.saasweb.pos.repository.MovimientoStockRepository;
import org.springframework.stereotype.Component;

/** Borra todo lo que el POS guardó para una tienda (baja definitiva). */
@Component
public class LimpiezaPos implements LimpiezaDeTienda {

    private final MovimientoStockRepository movimientos;
    private final ArticuloRepository articulos;
    private final CategoriaRepository categorias;

    public LimpiezaPos(MovimientoStockRepository movimientos, ArticuloRepository articulos,
                       CategoriaRepository categorias) {
        this.movimientos = movimientos;
        this.articulos = articulos;
        this.categorias = categorias;
    }

    @Override
    public void borrarDatosDe(String tenantId) {
        movimientos.deleteByTenantId(tenantId);
        articulos.deleteByTenantId(tenantId);
        categorias.deleteByTenantId(tenantId);
    }
}
