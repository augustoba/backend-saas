package com.saasweb.core.service;

import com.saasweb.core.common.TenantContext;
import com.saasweb.core.dto.SiteSettingsDtos;
import com.saasweb.core.model.*;
import com.saasweb.core.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Identidad y configuraciones del núcleo de cada tienda (una fila por tenant
 * en cada tabla {@code core_*}): identidad, ARCA, Mercado Pago, mail e
 * imágenes. Lo que es sólo del ecommerce (diseño, colores de la tienda,
 * medios de pago manuales, textos de ayuda) sigue en {@code SiteSettings}.
 *
 * <p>Los métodos sin tenantId usan el de la request ({@link TenantContext});
 * si la fila no existe, se crea con valores por defecto.</p>
 */
@Service
@Transactional
public class ConfiguracionService {

    private final IdentidadRepository identidades;
    private final ConfigArcaRepository arcas;
    private final ConfigMpRepository mps;
    private final ConfigMailRepository mails;
    private final ConfigImagenesRepository imagenes;

    public ConfiguracionService(IdentidadRepository identidades, ConfigArcaRepository arcas, ConfigMpRepository mps,
                                ConfigMailRepository mails, ConfigImagenesRepository imagenes) {
        this.identidades = identidades;
        this.arcas = arcas;
        this.mps = mps;
        this.mails = mails;
        this.imagenes = imagenes;
    }

    // ---------- lectura ----------

    public Identidad identidad() {
        return identidad(TenantContext.requireTenantId());
    }

    public Identidad identidad(String tenantId) {
        return identidades.findById(tenantId).orElseGet(() -> identidades.save(identidadPorDefecto(tenantId)));
    }

    public ConfigArca arca() {
        String id = TenantContext.requireTenantId();
        return arcas.findById(id).orElseGet(() -> {
            ConfigArca c = new ConfigArca();
            c.setId(id);
            return arcas.save(c);
        });
    }

    public ConfigMp mp() {
        return mp(TenantContext.requireTenantId());
    }

    public ConfigMp mp(String tenantId) {
        return mps.findById(tenantId).orElseGet(() -> {
            ConfigMp c = new ConfigMp();
            c.setId(tenantId);
            return mps.save(c);
        });
    }

    public ConfigMail mail() {
        String id = TenantContext.requireTenantId();
        return mails.findById(id).orElseGet(() -> {
            ConfigMail c = new ConfigMail();
            c.setId(id);
            return mails.save(c);
        });
    }

    public ConfigImagenes imagenes() {
        String id = TenantContext.requireTenantId();
        return imagenes.findById(id).orElseGet(() -> imagenes.save(imagenesPorDefecto(id)));
    }

    // ---------- alta de tienda ----------

    /**
     * Identidad de una tienda recién creada, con su nombre y logo (sin la marca
     * de la tienda piloto). La cuenta de imágenes se copia de una tienda que ya
     * tenga una cargada: hoy es una cuenta de plataforma, y sin esto la tienda
     * nueva no puede subir fotos hasta que el superadmin la configure.
     */
    public void crearPara(String tenantId, String storeName, String logoUrl) {
        Identidad i = new Identidad();
        i.setId(tenantId);
        i.setStoreName(storeName);
        i.setWhatsappNumber("5491100000000");
        i.setLogoUrl(logoUrl);
        identidades.save(i);
        imagenes.save(imagenesPorDefecto(tenantId));
    }

    /** Lo que junta el asistente "Crear tienda" sobre la identidad; null = dejar el valor por defecto. */
    public void aplicarIdentidadInicial(String tenantId, String brandColor, String whatsappNumber, String instagram,
                                        String facebookUrl, String logoUrl, String logoShape) {
        Identidad i = identidad(tenantId);
        if (brandColor != null) i.setBrandColor(brandColor);
        if (whatsappNumber != null) i.setWhatsappNumber(whatsappNumber);
        if (instagram != null) i.setInstagram(cleanHandle(instagram));
        if (facebookUrl != null) i.setFacebookUrl(facebookUrl);
        if (logoUrl != null) i.setLogoUrl(logoUrl);
        if (logoShape != null) i.setLogoShape(logoShape);
        identidades.save(i);
    }

    // ---------- edición ----------

    /** La parte de identidad del formulario "Identidad" del panel. */
    public Identidad actualizarIdentidad(SiteSettingsDtos.PlatformSettingsRequest req) {
        Identidad i = identidad();
        i.setStoreName(req.storeName().trim());
        i.setWhatsappNumber(req.whatsappNumber().trim());
        i.setAboutText(blankToNull(req.aboutText()));
        i.setInstagram(cleanHandle(req.instagram()));
        i.setFacebookUrl(blankToNull(req.facebookUrl()));
        i.setLogoUrl(blankToNull(req.logoUrl()));
        i.setLogoShape(blankToNull(req.logoShape()));
        i.setStoreAddress(blankToNull(req.storeAddress()));
        return identidades.save(i);
    }

    public Identidad actualizarColorDeMarca(String brandColor) {
        Identidad i = identidad();
        i.setBrandColor(blankToNull(brandColor));
        return identidades.save(i);
    }

    /** {@code accessToken} en blanco = no tocar el guardado. */
    public ConfigMp actualizarMp(SiteSettingsDtos.MercadoPagoConfigRequest req) {
        ConfigMp c = mp();
        c.setMpEnabled(Boolean.TRUE.equals(req.mpEnabled()));
        if (req.accessToken() != null && !req.accessToken().isBlank()) c.setMpAccessToken(req.accessToken().trim());
        c.setMpPublicKey(blankToNull(req.publicKey()));
        return mps.save(c);
    }

    /** Certificado y clave en blanco = no tocar los guardados. */
    public ConfigArca actualizarArca(SiteSettingsDtos.ArcaConfigRequest req) {
        ConfigArca c = arca();
        c.setArcaEnabled(Boolean.TRUE.equals(req.arcaEnabled()));
        if (req.arcaModoPrueba() != null) c.setArcaModoPrueba(req.arcaModoPrueba());
        c.setArcaCuit(blankToNull(req.cuit()));
        c.setArcaPuntoVenta(req.puntoVenta());
        c.setArcaCondicionIva(blankToNull(req.condicionIva()));
        if (req.certificadoPem() != null && !req.certificadoPem().isBlank()) c.setArcaCertificadoPem(req.certificadoPem().trim());
        if (req.clavePrivadaPem() != null && !req.clavePrivadaPem().isBlank()) c.setArcaClavePrivadaPem(req.clavePrivadaPem().trim());
        if (req.invoiceMode() != null && !req.invoiceMode().isBlank()) c.setInvoiceMode(req.invoiceMode());
        return arcas.save(c);
    }

    public ConfigImagenes actualizarImagenes(String cloudName, String uploadPreset) {
        ConfigImagenes c = imagenes();
        c.setCloudinaryCloudName(blankToNull(cloudName));
        c.setCloudinaryUploadPreset(blankToNull(uploadPreset));
        return imagenes.save(c);
    }

    /** {@code password} en blanco = no tocar la guardada. */
    public ConfigMail actualizarMail(String host, Integer port, String username, String password,
                                     String fromEmail, String fromName) {
        ConfigMail c = mail();
        c.setSmtpHost(blankToNull(host));
        c.setSmtpPort(port);
        c.setSmtpUsername(blankToNull(username));
        if (password != null && !password.isBlank()) c.setSmtpPassword(password);
        c.setSmtpFromEmail(blankToNull(fromEmail));
        c.setSmtpFromName(blankToNull(fromName));
        return mails.save(c);
    }

    /** Borra las filas de una tienda (baja definitiva). */
    public void borrarDe(String tenantId) {
        identidades.deleteById(tenantId);
        arcas.deleteById(tenantId);
        mps.deleteById(tenantId);
        mails.deleteById(tenantId);
        imagenes.deleteById(tenantId);
    }

    // ---------- defaults ----------

    /**
     * Identidad de la tienda piloto: es lo que se usaba como default cuando la
     * app era de una sola tienda. Las tiendas nuevas nacen con
     * {@link #crearPara}, así que esto sólo aplica a la piloto en una base vacía.
     */
    private static Identidad identidadPorDefecto(String tenantId) {
        Identidad i = new Identidad();
        i.setId(tenantId);
        i.setStoreName("Estilos Pequeños");
        i.setWhatsappNumber("5491122334455");
        i.setAboutText("Somos Estilos Pequeños 🧸 Hace 5 años vestimos a los más chicos con ropa cómoda, "
                + "de calidad y con onda. Elegimos cada prenda pensando en la comodidad de los peques "
                + "y la tranquilidad de las familias. ¡Gracias por elegirnos!");
        i.setInstagram("estilospequenos_");
        i.setFacebookUrl("https://www.facebook.com/share/1NZXdYgick/");
        return i;
    }

    private ConfigImagenes imagenesPorDefecto(String tenantId) {
        ConfigImagenes c = new ConfigImagenes();
        c.setId(tenantId);
        ConfigImagenes existente = imagenes.findAll().stream()
                .filter(x -> x.getCloudinaryCloudName() != null).findFirst().orElse(null);
        if (existente != null) {
            c.setCloudinaryCloudName(existente.getCloudinaryCloudName());
            c.setCloudinaryUploadPreset(existente.getCloudinaryUploadPreset());
        } else {
            // Cuenta histórica de la tienda piloto (antes hardcodeada en el front).
            c.setCloudinaryCloudName("jitutkbc");
            c.setCloudinaryUploadPreset("estilospequenos");
        }
        return c;
    }

    private static String blankToNull(String v) {
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    private static String cleanHandle(String v) {
        String h = blankToNull(v);
        return h == null ? null : h.replaceFirst("^@", "");
    }
}
