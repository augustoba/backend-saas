package com.saasweb.ecommerce.repository;

import com.saasweb.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {
    Optional<Product> findByIdAndTenantId(String id, String tenantId);
    List<Product> findByTenantId(String tenantId);

    /** Para "cargar producto por código de barras" desde el panel — no depende de qué página esté cargada. */
    Optional<Product> findByTenantIdAndBarcodeAndDeletedFalse(String tenantId, String barcode);

    /** Productos (no borrados) que tienen una variante con ese código de barras. */
    @org.springframework.data.jpa.repository.Query("select p from Product p join p.sizeStocks v"
            + " where p.tenantId = :tenantId and p.deleted = false and v.codigoBarras = :code")
    List<Product> findByVarianteBarcode(@org.springframework.data.repository.query.Param("tenantId") String tenantId,
                                        @org.springframework.data.repository.query.Param("code") String code);

    /** Borra los productos del tenant (y sus colecciones @ElementCollection) — ver TenantDeletionService. */
    void deleteAllByTenantId(String tenantId);
    /** Listado público y alertas de stock: variantes y parametrías en la misma consulta (sin N+1). */
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"sizeStocks", "params"})
    List<Product> findByTenantIdAndActiveTrueAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    /**
     * Portada (primera foto) de varios productos en una sola consulta, en vez de
     * cargar la galería entera de cada uno sólo para leer {@code images.get(0)}.
     * Filas {@code [productId, url]}.
     */
    @org.springframework.data.jpa.repository.Query(
            "select p.id, img from Product p join p.images img where p.id in :ids and index(img) = 0")
    List<Object[]> findCoverImages(@org.springframework.data.repository.query.Param("ids") java.util.Collection<String> ids);
    List<Product> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);
    List<Product> findByTenantIdAndDeletedTrueOrderByCreatedAtDesc(String tenantId);
    List<Product> findByTenantIdAndSupplierId(String tenantId, String supplierId);
    long countByTenantIdAndActiveTrueAndDeletedFalse(String tenantId);
    long countByTenantIdAndDeletedFalse(String tenantId);

    /**
     * Listado del panel con filtros opcionales (todos server-side):
     *  - `search`: texto en el nombre.
     *  - `supplierId`: proveedor exacto.
     *  - `active`: true = solo publicados, false = solo ocultos, null = todos.
     *  - `groupId` + `optionId`: el producto tiene esa opción de parametría.
     *  - `noStock`: true = solo productos con stock total 0.
     * Siempre excluye los archivados (deleted). Siempre acotado al tenant actual.
     */
    @Query("""
            select p from Product p
            where p.tenantId = :tenantId
              and p.deleted = false
              and (:search is null or lower(p.name) like :searchLike)
              and (:supplierId is null or p.supplierId = :supplierId)
              and (:active is null or p.active = :active)
              and (:groupId is null or exists (
                    select 1 from p.params pp where pp.groupId = :groupId and pp.optionId = :optionId))
              and (:noStock = false or not exists (
                    select 1 from p.sizeStocks ss where ss.stock > 0))
            order by p.createdAt desc
            """)
    Page<Product> search(@Param("tenantId") String tenantId,
                         @Param("search") String search,
                         @Param("searchLike") String searchLike,
                         @Param("supplierId") String supplierId,
                         @Param("active") Boolean active,
                         @Param("groupId") String groupId,
                         @Param("optionId") String optionId,
                         @Param("noStock") boolean noStock,
                         Pageable pageable);
}
