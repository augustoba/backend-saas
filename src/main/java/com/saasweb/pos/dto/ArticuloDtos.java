package com.saasweb.pos.dto;

import com.saasweb.pos.model.Articulo;
import com.saasweb.pos.model.Categoria;
import com.saasweb.pos.model.MovimientoStock;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public final class ArticuloDtos {

    private ArticuloDtos() {}

    public record ArticuloRequest(
            @NotBlank @Size(max = 200) String nombre,
            @Size(max = 64) String codigoBarras,
            @NotNull @DecimalMin("0") BigDecimal precio,
            @DecimalMin("0") BigDecimal costo,
            @DecimalMin("0") BigDecimal iva,
            /** Sólo al crear: stock inicial (después se mueve con ingresos/ajustes). */
            Integer stockInicial,
            Integer umbralStock,
            String categoriaId
    ) {}

    public record ArticuloResponse(
            String id, String nombre, String codigoBarras, BigDecimal precio, BigDecimal costo, BigDecimal iva,
            int stock, Integer umbralStock, boolean stockBajo, String categoriaId, boolean archivado, Instant creadoEn
    ) {
        public static ArticuloResponse from(Articulo a) {
            return new ArticuloResponse(a.getId(), a.getNombre(), a.getCodigoBarras(), a.getPrecio(), a.getCosto(),
                    a.getIva(), a.getStock(), a.getUmbralStock(), a.stockBajo(), a.getCategoriaId(), a.isArchivado(),
                    a.getCreadoEn());
        }
    }

    /** Ingreso de mercadería (cantidad > 0) o ajuste por conteo (cantidad = stock contado). */
    public record StockRequest(
            @NotNull MovimientoStock.Motivo motivo,
            @NotNull Integer cantidad,
            @DecimalMin("0") BigDecimal costoUnitario,
            @Size(max = 500) String nota
    ) {}

    public record MovimientoResponse(
            String id, String articuloId, String articuloNombre, int cantidad, int stockResultante,
            MovimientoStock.Motivo motivo, String referenciaId, String nota, BigDecimal costoUnitario,
            String autorNombre, Instant fecha
    ) {
        public static MovimientoResponse from(MovimientoStock m) {
            return new MovimientoResponse(m.getId(), m.getArticuloId(), m.getArticuloNombre(), m.getCantidad(),
                    m.getStockResultante(), m.getMotivo(), m.getReferenciaId(), m.getNota(), m.getCostoUnitario(),
                    m.getAutorNombre(), m.getFecha());
        }
    }

    public record CategoriaRequest(@NotBlank @Size(max = 100) String nombre, Integer posicion) {}

    public record CategoriaResponse(String id, String nombre, int posicion) {
        public static CategoriaResponse from(Categoria c) {
            return new CategoriaResponse(c.getId(), c.getNombre(), c.getPosicion());
        }
    }
}
