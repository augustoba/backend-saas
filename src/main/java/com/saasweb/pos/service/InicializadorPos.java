package com.saasweb.pos.service;

import com.saasweb.core.dto.TenantAdminDtos.TenantCreateRequest;
import com.saasweb.core.model.Sistema;
import com.saasweb.core.model.Tenant;
import com.saasweb.core.service.InicializadorDeSistema;
import com.saasweb.pos.model.Articulo;
import com.saasweb.pos.model.Categoria;
import com.saasweb.pos.model.MovimientoStock;
import com.saasweb.pos.repository.ArticuloRepository;
import com.saasweb.pos.repository.CategoriaRepository;
import com.saasweb.pos.repository.MovimientoStockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Siembra un punto de venta recién creado con categorías y artículos de
 * kiosco (con código de barras), para poder vender escaneando desde el
 * primer minuto. Es directo por id: durante el alta la request todavía está
 * en el contexto del superadmin.
 */
@Service
@Transactional
public class InicializadorPos implements InicializadorDeSistema {

    private final CategoriaRepository categorias;
    private final ArticuloRepository articulos;
    private final MovimientoStockRepository movimientos;

    public InicializadorPos(CategoriaRepository categorias, ArticuloRepository articulos,
                            MovimientoStockRepository movimientos) {
        this.categorias = categorias;
        this.articulos = articulos;
        this.movimientos = movimientos;
    }

    @Override
    public boolean aplicaA(Sistema sistema) {
        return sistema == Sistema.POS;
    }

    @Override
    public void inicializar(Tenant tenant, TenantCreateRequest req) {
        sembrar(tenant.getId());
    }

    /** También lo usa el arranque para los puntos de venta que todavía no tienen artículos. */
    public void sembrar(String tenantId) {
        String bebidas = categoria(tenantId, "Bebidas", 0);
        String golosinas = categoria(tenantId, "Golosinas", 1);
        String almacen = categoria(tenantId, "Almacén", 2);
        articulo(tenantId, bebidas, "Agua mineral 500 ml", "7790895000997", 900, 520, 24);
        articulo(tenantId, bebidas, "Gaseosa cola 500 ml", "7790895067556", 1500, 900, 24);
        articulo(tenantId, bebidas, "Jugo en caja 200 ml", "7790036001623", 700, 380, 18);
        articulo(tenantId, golosinas, "Alfajor triple", "7790040113003", 1200, 700, 30);
        articulo(tenantId, golosinas, "Chicles menta", "7790580120108", 600, 300, 40);
        articulo(tenantId, golosinas, "Chocolate con leche 25 g", "7790580387709", 1000, 560, 20);
        articulo(tenantId, almacen, "Galletitas de agua", "7790040929901", 1300, 780, 12);
        articulo(tenantId, almacen, "Pañuelos descartables", null, 800, 450, 10);
    }

    private String categoria(String tenantId, String nombre, int posicion) {
        Categoria c = new Categoria();
        c.setId(UUID.randomUUID().toString());
        c.setTenantId(tenantId);
        c.setNombre(nombre);
        c.setPosicion(posicion);
        return categorias.save(c).getId();
    }

    private void articulo(String tenantId, String categoriaId, String nombre, String codigo,
                          int precio, int costo, int stock) {
        Articulo a = new Articulo();
        a.setId(UUID.randomUUID().toString());
        a.setTenantId(tenantId);
        a.setCategoriaId(categoriaId);
        a.setNombre(nombre);
        a.setCodigoBarras(codigo);
        a.setPrecio(BigDecimal.valueOf(precio));
        a.setCosto(BigDecimal.valueOf(costo));
        a.setStock(stock);
        a.setUmbralStock(5);
        articulos.save(a);
        MovimientoStock m = new MovimientoStock();
        m.setId(UUID.randomUUID().toString());
        m.setTenantId(tenantId);
        m.setArticuloId(a.getId());
        m.setArticuloNombre(nombre);
        m.setCantidad(stock);
        m.setStockResultante(stock);
        m.setMotivo(MovimientoStock.Motivo.INGRESO);
        m.setNota("Stock inicial de ejemplo");
        movimientos.save(m);
    }
}
