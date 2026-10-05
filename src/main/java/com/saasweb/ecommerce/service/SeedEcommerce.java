package com.saasweb.ecommerce.service;

import com.saasweb.core.config.AppProperties;
import com.saasweb.core.common.TenantContext;
import com.saasweb.ecommerce.model.Discount;
import com.saasweb.ecommerce.model.HeroSlide;
import com.saasweb.ecommerce.repository.HeroSlideRepository;
import com.saasweb.ecommerce.model.Order;
import com.saasweb.ecommerce.model.OrderLine;
import com.saasweb.ecommerce.repository.OrderRepository;
import com.saasweb.ecommerce.model.OrderStatus;
import com.saasweb.ecommerce.model.ParamGroup;
import com.saasweb.ecommerce.model.ParamOption;
import com.saasweb.ecommerce.model.Product;
import com.saasweb.ecommerce.model.ProductParam;
import com.saasweb.core.repository.IdentidadRepository;
import com.saasweb.ecommerce.repository.SiteSettingsRepository;
import com.saasweb.core.service.RubroImages;
import com.saasweb.core.model.Tenant;
import com.saasweb.ecommerce.model.SizeScale;
import com.saasweb.ecommerce.model.Variante;
import com.saasweb.ecommerce.repository.DiscountRepository;
import com.saasweb.ecommerce.repository.ParamRepository;
import com.saasweb.ecommerce.repository.ProductRepository;
import com.saasweb.ecommerce.repository.SizeScaleRepository;
import com.saasweb.ecommerce.service.PageBlockService;
import com.saasweb.ecommerce.service.SiteSettingsService;
import com.saasweb.core.service.TenantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Datos del ecommerce de la tienda piloto: su configuración de tienda online,
 * los bloques de la home y, la primera vez, productos, talles, parametrías y
 * descuentos de ejemplo; además los backfills del ecommerce. Corre después del
 * {@code DataSeeder} del núcleo (que crea la tienda, sus roles y usuarios).
 */
@Component
@org.springframework.core.annotation.Order(2)
public class SeedEcommerce implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedEcommerce.class);

    private final AppProperties props;
    private final SiteSettingsService siteSettingsService;
    private final TenantService tenantService;
    private final PageBlockService pageBlockService;
    private final ParamRepository paramRepo;
    private final SizeScaleRepository sizeScaleRepo;
    private final DiscountRepository discountRepo;
    private final ProductRepository productRepo;
    private final HeroSlideRepository heroSlideRepo;
    private final SiteSettingsRepository siteSettingsRepo;
    private final IdentidadRepository identidadRepo;
    private final OrderRepository orderRepo;

    public SeedEcommerce(AppProperties props,
                      SiteSettingsService siteSettingsService,
                      TenantService tenantService,
                      PageBlockService pageBlockService,
                      ParamRepository paramRepo,
                      SizeScaleRepository sizeScaleRepo,
                      DiscountRepository discountRepo,
                      ProductRepository productRepo,
                      HeroSlideRepository heroSlideRepo,
                      SiteSettingsRepository siteSettingsRepo, IdentidadRepository identidadRepo,
                      OrderRepository orderRepo) {
        this.props = props;
        this.siteSettingsService = siteSettingsService;
        this.tenantService = tenantService;
        this.pageBlockService = pageBlockService;
        this.paramRepo = paramRepo;
        this.sizeScaleRepo = sizeScaleRepo;
        this.discountRepo = discountRepo;
        this.productRepo = productRepo;
        this.heroSlideRepo = heroSlideRepo;
        this.siteSettingsRepo = siteSettingsRepo;
        this.identidadRepo = identidadRepo;
        this.orderRepo = orderRepo;
    }

    /**
     * En una transacción: los backfills recorren colecciones lazy (ej: las líneas
     * de los pedidos) y fuera de una sesión de Hibernate fallan.
     */
    @Override
    @org.springframework.transaction.annotation.Transactional
    public void run(String... args) {
        String tenantId = tenantService.resolveIdBySlug(props.getTenant().getSlug());
        if (tenantId == null) return;
        TenantContext.set(tenantId);
        try {
            siteSettingsService.get();
            pageBlockService.ensureDefaultHomeBlocks(tenantId);

            if (!props.getSeed().isEnabled()) return;
            seedParamGroups(tenantId);
            seedSizeScales(tenantId);
            seedDiscounts(tenantId);
            seedProducts(tenantId);
        } finally {
            TenantContext.clear();
        }

        backfillHeroSlidesAndLogos();
        backfillExpenseCategoryGroup();
        backfillOrderLineCosts();
    }

    /**
     * Completa la parametría "Categoría de gasto" (id fijo {@code
     * grp-categoria-gasto}) para tenants que ya existían antes de que el
     * módulo de Gastos empezara a sembrarla en {@code TenantProvisioningService}
     * — corre en cada arranque, sin efecto si el tenant ya la tiene.
     */
    private void backfillExpenseCategoryGroup() {
        for (Tenant t : tenantService.findAll()) {
            if (paramRepo.findByIdAndTenantId("grp-categoria-gasto", t.getId()).isPresent()) continue;
            paramRepo.save(group(t.getId(), "grp-categoria-gasto", "Categoría de gasto", false, false, false, List.of(
                    opt("gasto-alquiler", "Alquiler"), opt("gasto-sueldos", "Sueldos"),
                    opt("gasto-servicios", "Servicios"), opt("gasto-mercaderia", "Mercadería / Insumos"),
                    opt("gasto-impuestos", "Impuestos"), opt("gasto-marketing", "Marketing"),
                    opt("gasto-otros", "Otros"))));
            log.info("Seed: categorías de gasto completadas para tenant '{}'.", t.getSlug());
        }
    }

    /**
     * Congela {@code OrderLine.costPrice} en pedidos ya {@code PROCESADO} de
     * antes de que existiera este campo, con el {@code costPrice} ACTUAL del
     * producto (marcado {@code costEstimated=true}, no es el costo real de
     * esa venta). Corre en cada arranque, sólo toca líneas con costo nulo —
     * no-op una vez completado. Ver PLAN_SAAS.md, módulo de Gastos y Balance.
     */
    private void backfillOrderLineCosts() {
        for (Tenant t : tenantService.findAll()) {
            for (Order o : orderRepo.findByTenantIdAndStatus(t.getId(), OrderStatus.PROCESADO)) {
                boolean changed = false;
                for (OrderLine l : o.getLines()) {
                    if (l.getCostPrice() != null) continue;
                    Product p = productRepo.findByIdAndTenantId(l.getProductId(), t.getId()).orElse(null);
                    if (p == null || p.getCostPrice() == null) continue;
                    l.setCostPrice(p.getCostPrice());
                    l.setCostEstimated(true);
                    changed = true;
                }
                if (changed) orderRepo.save(o);
            }
        }
    }

    /**
     * Completa carrusel/logo de tenants que ya existían de antes de que
     * {@code TenantProvisioningService} empezara a sembrarlos (ver
     * PLAN_SAAS.md Fase 9) — corre en cada arranque, sin efecto si ya están
     * completos. La tienda piloto (theme "default") no se toca: su logo
     * real lo maneja el fallback estático del frontend
     * ({@code LOGO_FALLBACK}), no este seeder.
     */
    private void backfillHeroSlidesAndLogos() {
        for (Tenant t : tenantService.findAll()) {
            if (heroSlideRepo.countByTenantId(t.getId()) == 0) {
                List<RubroImages.Slide> slides = RubroImages.heroSlidesFor(t.getRubro());
                for (int i = 0; i < slides.size(); i++) {
                    RubroImages.Slide slide = slides.get(i);
                    HeroSlide hs = new HeroSlide();
                    hs.setId(UUID.randomUUID().toString());
                    hs.setTenantId(t.getId());
                    hs.setImageUrl(slide.imageDataUri());
                    hs.setAlt(slide.alt());
                    hs.setPosition(i);
                    heroSlideRepo.save(hs);
                }
                log.info("Seed: carrusel completado para tenant '{}' ({} fotos).", t.getSlug(), slides.size());
            }

            boolean themeDeRubro = siteSettingsRepo.findById(t.getId())
                    .map(s -> !"default".equals(s.getTheme())).orElse(false);
            identidadRepo.findById(t.getId()).ifPresent(i -> {
                if (i.getLogoUrl() == null && themeDeRubro) {
                    i.setLogoUrl(RubroImages.logo(t.getRubro().getLogoEmoji(), t.getRubro().getLogoColor()));
                    identidadRepo.save(i);
                    log.info("Seed: logo completado para tenant '{}'.", t.getSlug());
                }
            });
        }
    }

    // --- Parametrías ---

    private void seedParamGroups(String tenantId) {
        if (!paramRepo.findByTenantId(tenantId).isEmpty()) return;
        paramRepo.save(group(tenantId, "grp-publico", "Público", false, true, true, List.of(
                opt("publico-bebe", "Bebé"), opt("publico-nena", "Nena"),
                opt("publico-nene", "Nene"), opt("publico-unisex", "Unisex"))));
        paramRepo.save(group(tenantId, "grp-tipo", "Tipo de prenda", false, true, false, List.of(
                opt("tipo-remera", "Remera"), opt("tipo-buzo", "Buzo / Campera"),
                opt("tipo-pantalon", "Pantalón"), opt("tipo-jean", "Jean"),
                opt("tipo-vestido", "Vestido / Pollera"), opt("tipo-body", "Body / Enterito"),
                opt("tipo-conjunto", "Conjunto"), opt("tipo-calzado", "Calzado"),
                opt("tipo-accesorio", "Accesorio"))));
        paramRepo.save(group(tenantId, "grp-estacion", "Estación", true, true, false, List.of(
                opt("estacion-primavera", "Primavera"), opt("estacion-verano", "Verano"),
                opt("estacion-otono", "Otoño"), opt("estacion-invierno", "Invierno"),
                opt("estacion-todo", "Todo el año"))));
        log.info("Seed: 3 parametrías cargadas.");
    }

    private ParamGroup group(String tenantId, String id, String name, boolean multiple, boolean showInCatalog,
                             boolean system, List<ParamOption> options) {
        ParamGroup g = new ParamGroup();
        g.setId(id);
        g.setTenantId(tenantId);
        g.setName(name);
        g.setMultiple(multiple);
        g.setShowInCatalog(showInCatalog);
        g.setSystem(system);
        options.forEach(g::addOption);
        return g;
    }

    private ParamOption opt(String id, String label) {
        ParamOption o = new ParamOption();
        o.setId(id);
        o.setLabel(label);
        return o;
    }

    // --- Escalas de talle ---

    private void seedSizeScales(String tenantId) {
        if (!sizeScaleRepo.findByTenantId(tenantId).isEmpty()) return;
        sizeScaleRepo.save(scale(tenantId, "escala-bebe", "Ropa bebé (por edad)",
                List.of("RN", "0-3M", "3-6M", "6-12M", "12-18M", "18-24M", "24M")));
        sizeScaleRepo.save(scale(tenantId, "escala-ninos", "Ropa niños",
                List.of("1", "2", "3", "4", "6", "8", "10", "12", "14", "16")));
        sizeScaleRepo.save(scale(tenantId, "escala-adultos", "Ropa adultos",
                List.of("XS", "S", "M", "L", "XL", "XXL")));
        sizeScaleRepo.save(scale(tenantId, "escala-calzado-ninos", "Calzado niños", range(17, 34)));
        sizeScaleRepo.save(scale(tenantId, "escala-calzado-adultos", "Calzado adultos", range(34, 46)));
        log.info("Seed: 5 escalas de talle cargadas.");
    }

    private SizeScale scale(String tenantId, String id, String name, List<String> values) {
        SizeScale s = new SizeScale();
        s.setId(id);
        s.setTenantId(tenantId);
        s.setName(name);
        s.setSystem(true);
        s.setValues(new ArrayList<>(values));
        return s;
    }

    private static List<String> range(int from, int to) {
        return IntStream.rangeClosed(from, to).mapToObj(String::valueOf).collect(Collectors.toList());
    }

    // --- Descuentos ---

    private void seedDiscounts(String tenantId) {
        if (discountRepo.findByTenantId(tenantId).isEmpty()) {
            discountRepo.save(montoTier(tenantId, "100000", 20));
            discountRepo.save(montoTier(tenantId, "200000", 25));
            log.info("Seed: 2 descuentos por monto cargados.");
        }
    }

    private Discount montoTier(String tenantId, String minAmount, int percent) {
        Discount d = new Discount();
        d.setId(UUID.randomUUID().toString());
        d.setTenantId(tenantId);
        d.setKind(Discount.Kind.MONTO);
        d.setMinAmount(new BigDecimal(minAmount));
        d.setDiscountPercent(percent);
        d.setEnabled(true);
        return d;
    }

    // --- Productos de ejemplo ---

    private void seedProducts(String tenantId) {
        if (!productRepo.findByTenantId(tenantId).isEmpty()) return;

        productRepo.save(product(tenantId, "Body manga larga estampado animales",
                "Body de algodón suave, manga larga, con estampa de animalitos. Ideal para el día a día.",
                "9800", "0 a 12 meses", "escala-bebe", "🧸", "#fdba74",
                params("publico-bebe", "tipo-body", List.of("estacion-todo")),
                stocks("RN:4", "0-3M:3", "3-6M:4", "6-12M:3")));

        productRepo.save(product(tenantId, "Conjunto jogging campera + pantalón",
                "Conjunto de frisa perchada, campera con capucha y pantalón con puños. Súper abrigado.",
                "24500", "2 a 8 años", "escala-ninos", "🧥", "#85d6ff",
                params("publico-nene", "tipo-conjunto", List.of("estacion-otono", "estacion-invierno")),
                stocks("2:2", "3:2", "4:3", "6:1", "8:1")));

        productRepo.save(product(tenantId, "Vestido plumeti volados",
                "Vestido liviano de tela plumeti con volados en el ruedo y moño en la espalda.",
                "19900", "2 a 10 años", "escala-ninos", "👗", "#f9a8d4",
                params("publico-nena", "tipo-vestido", List.of("estacion-primavera", "estacion-verano")),
                stocks("2:2", "3:2", "4:3", "6:2", "8:1", "10:1")));

        productRepo.save(product(tenantId, "Remera básica algodón (pack x3)",
                "Pack de 3 remeras lisas de algodón peinado en colores surtidos. Unisex.",
                "15600", "1 a 12 años", "escala-ninos", "👕", "#86e6bb",
                params("publico-unisex", "tipo-remera", List.of("estacion-todo")),
                stocks("1:3", "2:3", "3:3", "4:4", "6:2", "8:2", "10:2", "12:1")));

        productRepo.save(product(tenantId, "Jean chupín con elástico",
                "Jean chupín de tiro medio con cintura elastizada para mayor comodidad.",
                "21300", "2 a 12 años", "escala-ninos", "👖", "#60a5fa",
                params("publico-nena", "tipo-jean", List.of("estacion-otono", "estacion-invierno", "estacion-primavera")),
                stocks("2:1", "3:1", "4:2", "6:1", "8:1", "10:1", "12:0")));

        productRepo.save(product(tenantId, "Buzo canguro dinosaurios",
                "Buzo canguro de frisa con bolsillo y estampa de dinosaurios.",
                "18200", "2 a 10 años", "escala-ninos", "🦕", "#fde68a",
                params("publico-nene", "tipo-buzo", List.of("estacion-otono", "estacion-invierno")),
                stocks("2:0", "3:0", "4:0", "6:0", "8:0", "10:0")));

        productRepo.save(product(tenantId, "Enterito corto verano",
                "Enterito liviano de algodón, ideal para el verano, con broches en la entrepierna.",
                "13400", "3 a 24 meses", "escala-bebe", "🌞", "#fca5a5",
                params("publico-bebe", "tipo-body", List.of("estacion-primavera", "estacion-verano")),
                stocks("3-6M:5", "6-12M:6", "12-18M:3", "18-24M:2")));

        productRepo.save(product(tenantId, "Campera inflable con capucha",
                "Campera inflable liviana, abrigada, con capucha desmontable. Repelente al agua.",
                "32900", "4 a 14 años", "escala-ninos", "🧥", "#a5b4fc",
                params("publico-unisex", "tipo-buzo", List.of("estacion-otono", "estacion-invierno")),
                stocks("4:1", "6:1", "8:1", "10:1", "12:1", "14:0")));

        productRepo.save(product(tenantId, "Pollera short con volado",
                "Pollera short de gabardina liviana con volado, cintura con elástico.",
                "12800", "2 a 10 años", "escala-ninos", "🩳", "#f0abfc",
                params("publico-nena", "tipo-vestido", List.of("estacion-primavera", "estacion-verano")),
                stocks("2:2", "3:2", "4:2", "6:2", "8:1", "10:1")));

        productRepo.save(product(tenantId, "Zapatillas urbanas velcro",
                "Zapatillas livianas con cierre de velcro, suela antideslizante.",
                "27500", "1 a 8 años", "escala-calzado-ninos", "👟", "#d8b4fe",
                params("publico-unisex", "tipo-calzado", List.of("estacion-todo")),
                stocks("22:1", "24:2", "26:2", "28:1", "30:1", "32:1")));

        log.info("Seed: 10 productos de ejemplo cargados.");
    }

    private Product product(String tenantId, String name, String description, String price, String ageRange,
                            String sizeScaleId, String emoji, String color,
                            Set<ProductParam> params, List<Variante> stocks) {
        Product p = new Product();
        p.setId(UUID.randomUUID().toString());
        p.setTenantId(tenantId);
        p.setName(name);
        p.setDescription(description);
        p.setPrice(new BigDecimal(price));
        p.setAgeRange(ageRange);
        // 2 imágenes de ejemplo (portada + una alternativa) para ver la galería
        p.getImages().add(iconDataUri(emoji, color));
        p.getImages().add(iconDataUri(emoji, "#e7e5e4"));
        p.setActive(true);
        p.setSizeScaleId(sizeScaleId);
        p.getParams().addAll(params);
        p.getSizeStocks().addAll(stocks);
        return p;
    }

    private Set<ProductParam> params(String publico, String tipo, List<String> estaciones) {
        Set<ProductParam> set = new LinkedHashSet<>();
        set.add(new ProductParam("grp-publico", publico));
        set.add(new ProductParam("grp-tipo", tipo));
        for (String e : estaciones) set.add(new ProductParam("grp-estacion", e));
        return set;
    }

    private List<Variante> stocks(String... pairs) {
        List<Variante> list = new ArrayList<>();
        for (String pair : pairs) {
            String[] parts = pair.split(":");
            list.add(new Variante(parts[0], Integer.parseInt(parts[1])));
        }
        return list;
    }

    /** SVG mínimo (emoji sobre círculo de color) como data URI, para el catálogo de ejemplo. */
    private String iconDataUri(String emoji, String color) {
        String svg = "<svg xmlns='http://www.w3.org/2000/svg' width='400' height='500' viewBox='0 0 400 500'>"
                + "<rect width='400' height='500' fill='#fff7ed'/>"
                + "<circle cx='200' cy='250' r='150' fill='" + color + "'/>"
                + "<text x='200' y='300' font-size='140' text-anchor='middle'>" + emoji + "</text></svg>";
        return "data:image/svg+xml;charset=utf-8," + URLEncoder.encode(svg, StandardCharsets.UTF_8)
                .replace("+", "%20");
    }
}
