package com.saasweb.pos.service;

import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;
import com.saasweb.pos.dto.ArticuloDtos.ArticuloRequest;
import com.saasweb.pos.dto.ArticuloDtos.CategoriaRequest;
import com.saasweb.pos.dto.ArticuloDtos.StockRequest;
import com.saasweb.pos.model.Articulo;
import com.saasweb.pos.model.Categoria;
import com.saasweb.pos.model.MovimientoStock;
import com.saasweb.pos.repository.ArticuloRepository;
import com.saasweb.pos.repository.CategoriaRepository;
import com.saasweb.pos.repository.MovimientoStockRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Artículos, categorías y stock del POS. Todo cambio de stock pasa por {@link #moverStock}. */
@Service
@Transactional
public class ArticuloService {

    private final ArticuloRepository articulos;
    private final CategoriaRepository categorias;
    private final MovimientoStockRepository movimientos;
    private final Cajeros cajeros;

    public ArticuloService(ArticuloRepository articulos, CategoriaRepository categorias,
                           MovimientoStockRepository movimientos, Cajeros cajeros) {
        this.articulos = articulos;
        this.categorias = categorias;
        this.movimientos = movimientos;
        this.cajeros = cajeros;
    }

    // --- Artículos ---

    @Transactional(readOnly = true)
    public List<Articulo> listar(boolean archivados) {
        return articulos.findByTenantIdAndArchivadoOrderByNombreAsc(TenantContext.getTenantId(), archivados);
    }

    @Transactional(readOnly = true)
    public Articulo get(String id) {
        return articulos.findByIdAndTenantId(id, TenantContext.getTenantId())
                .orElseThrow(() -> ResourceNotFoundException.of("Artículo", id));
    }

    @Transactional(readOnly = true)
    public Articulo porCodigo(String codigo) {
        return articulos.findByTenantIdAndCodigoBarras(TenantContext.getTenantId(), codigo.trim())
                .filter(a -> !a.isArchivado())
                .orElseThrow(() -> ResourceNotFoundException.of("Artículo con código", codigo));
    }

    public Articulo crear(ArticuloRequest req, String autorDni) {
        Articulo a = new Articulo();
        a.setId(UUID.randomUUID().toString());
        a.setTenantId(TenantContext.getTenantId());
        aplicar(a, req);
        // Con id asignado a mano, save() hace merge y devuelve OTRA instancia: el stock
        // inicial se aplica sobre la administrada, si no se pierde.
        a = articulos.save(a);
        int inicial = req.stockInicial() == null ? 0 : req.stockInicial();
        if (inicial != 0) moverStock(a, inicial, MovimientoStock.Motivo.INGRESO, null, "Stock inicial", a.getCosto(), autorDni);
        return a;
    }

    public Articulo actualizar(String id, ArticuloRequest req) {
        Articulo a = get(id);
        aplicar(a, req);
        return articulos.save(a);
    }

    public Articulo archivar(String id, boolean archivado) {
        Articulo a = get(id);
        a.setArchivado(archivado);
        return articulos.save(a);
    }

    private void aplicar(Articulo a, ArticuloRequest req) {
        String codigo = blankToNull(req.codigoBarras());
        if (codigo != null) {
            articulos.findByTenantIdAndCodigoBarras(a.getTenantId(), codigo)
                    .filter(otro -> !otro.getId().equals(a.getId()))
                    .ifPresent(otro -> {
                        throw new BadRequestException("El código " + codigo + " ya es de \"" + otro.getNombre() + "\".");
                    });
        }
        String categoriaId = blankToNull(req.categoriaId());
        if (categoriaId != null) getCategoria(categoriaId);
        a.setNombre(req.nombre().trim());
        a.setCodigoBarras(codigo);
        a.setPrecio(req.precio());
        a.setCosto(req.costo());
        a.setIva(req.iva());
        a.setUmbralStock(req.umbralStock());
        a.setCategoriaId(categoriaId);
    }

    // --- Stock ---

    /**
     * Ingreso de mercadería (suma {@code cantidad}) o ajuste por conteo
     * ({@code cantidad} = lo que se contó; se registra la diferencia).
     */
    public Articulo cambiarStock(String id, StockRequest req, String autorDni) {
        Articulo a = get(id);
        int delta = switch (req.motivo()) {
            case INGRESO -> {
                if (req.cantidad() <= 0) throw new BadRequestException("El ingreso tiene que ser de al menos 1 unidad.");
                yield req.cantidad();
            }
            case AJUSTE -> req.cantidad() - a.getStock();
            default -> throw new BadRequestException("Las ventas y anulaciones mueven el stock solas.");
        };
        if (delta == 0) return a;
        moverStock(a, delta, req.motivo(), null, blankToNull(req.nota()), req.costoUnitario(), autorDni);
        if (req.motivo() == MovimientoStock.Motivo.INGRESO && req.costoUnitario() != null) a.setCosto(req.costoUnitario());
        return articulos.save(a);
    }

    /** Único punto que toca el stock: lo cambia y deja el movimiento con el stock resultante. */
    public void moverStock(Articulo a, int delta, MovimientoStock.Motivo motivo, String referenciaId, String nota,
                           BigDecimal costoUnitario, String autorDni) {
        a.setStock(a.getStock() + delta);
        MovimientoStock m = new MovimientoStock();
        m.setId(UUID.randomUUID().toString());
        m.setTenantId(a.getTenantId());
        m.setArticuloId(a.getId());
        m.setArticuloNombre(a.getNombre());
        m.setCantidad(delta);
        m.setStockResultante(a.getStock());
        m.setMotivo(motivo);
        m.setReferenciaId(referenciaId);
        m.setNota(nota);
        m.setCostoUnitario(costoUnitario);
        m.setAutorDni(autorDni);
        m.setAutorNombre(cajeros.nombreDe(autorDni));
        movimientos.save(m);
    }

    @Transactional(readOnly = true)
    public List<MovimientoStock> movimientos(String articuloId, int limite) {
        PageRequest page = PageRequest.of(0, Math.min(Math.max(limite, 1), 500));
        String tenantId = TenantContext.getTenantId();
        return articuloId == null
                ? movimientos.findByTenantIdOrderByFechaDesc(tenantId, page)
                : movimientos.findByTenantIdAndArticuloIdOrderByFechaDesc(tenantId, articuloId, page);
    }

    // --- Categorías ---

    @Transactional(readOnly = true)
    public List<Categoria> categorias() {
        return categorias.findByTenantIdOrderByPosicionAscNombreAsc(TenantContext.getTenantId());
    }

    @Transactional(readOnly = true)
    public Categoria getCategoria(String id) {
        return categorias.findByIdAndTenantId(id, TenantContext.getTenantId())
                .orElseThrow(() -> ResourceNotFoundException.of("Categoría", id));
    }

    public Categoria crearCategoria(CategoriaRequest req) {
        Categoria c = new Categoria();
        c.setId(UUID.randomUUID().toString());
        c.setTenantId(TenantContext.getTenantId());
        c.setNombre(req.nombre().trim());
        c.setPosicion(req.posicion() == null ? 0 : req.posicion());
        return categorias.save(c);
    }

    public Categoria actualizarCategoria(String id, CategoriaRequest req) {
        Categoria c = getCategoria(id);
        c.setNombre(req.nombre().trim());
        if (req.posicion() != null) c.setPosicion(req.posicion());
        return categorias.save(c);
    }

    public void borrarCategoria(String id) {
        Categoria c = getCategoria(id);
        if (articulos.countByTenantIdAndCategoriaId(c.getTenantId(), id) > 0) {
            throw new BadRequestException("La categoría tiene artículos: pasalos a otra antes de borrarla.");
        }
        categorias.delete(c);
    }

    private static String blankToNull(String v) {
        return (v == null || v.isBlank()) ? null : v.trim();
    }
}
