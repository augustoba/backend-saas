package com.saasweb.core.service;

import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.TenantContext;
import com.saasweb.core.model.ConfigImagenes;
import com.saasweb.core.repository.TenantRepository;
import com.saasweb.core.service.ProveedorImagenes.TicketSubida;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Subida de imágenes de cualquier sistema: elige el proveedor de la tienda
 * ({@link ProveedorImagenes}) y arma el ticket. La carpeta siempre queda
 * debajo del slug de la tienda, así las fotos de una tienda nunca se mezclan
 * con las de otra.
 */
@Service
public class ImagenService {

    /** Segmentos de carpeta/nombre: minúsculas, números, guiones y guión bajo. */
    private static final Pattern SEGMENTO = Pattern.compile("^[a-z0-9][a-z0-9_-]{0,79}$");

    private final List<ProveedorImagenes> proveedores;
    private final ConfiguracionService configuracion;
    private final TenantRepository tenants;

    public ImagenService(List<ProveedorImagenes> proveedores, ConfiguracionService configuracion,
                         TenantRepository tenants) {
        this.proveedores = proveedores;
        this.configuracion = configuracion;
        this.tenants = tenants;
    }

    /**
     * @param carpeta     relativa a la tienda (ej: "productos", "pagos")
     * @param nombre      nombre del archivo sin extensión; null = lo elige el proveedor
     * @param slugDestino tienda destino distinta de la actual (sólo la usa el
     *                    superadmin desde "Crear tienda", para el logo de una
     *                    tienda que todavía no existe); null = la tienda actual
     */
    public TicketSubida ticket(String carpeta, String nombre, String slugDestino) {
        ConfigImagenes cfg = configuracion.imagenes();
        ProveedorImagenes proveedor = proveedorDe(cfg);
        if (!proveedor.configurado(cfg)) {
            throw new BadRequestException("La subida de imágenes todavía no está configurada (la carga el superadmin).");
        }
        String tienda = slugDestino != null ? slugDestino : tenants.findById(TenantContext.requireTenantId())
                .orElseThrow().getSlug();
        String ruta = segmento(tienda, "tienda");
        for (String parte : (carpeta == null ? "" : carpeta).split("/")) {
            if (!parte.isBlank()) ruta += "/" + segmento(parte, "carpeta");
        }
        return proveedor.ticket(cfg, ruta, nombre == null || nombre.isBlank() ? null : segmento(nombre, "nombre"));
    }

    private ProveedorImagenes proveedorDe(ConfigImagenes cfg) {
        String clave = cfg.getProveedor() == null ? CloudinaryImagenes.CLAVE : cfg.getProveedor();
        return proveedores.stream().filter(p -> p.clave().equals(clave)).findFirst()
                .orElseThrow(() -> new IllegalStateException("Proveedor de imágenes desconocido: " + clave));
    }

    private static String segmento(String v, String que) {
        String s = v.trim().toLowerCase(Locale.ROOT);
        if (!SEGMENTO.matcher(s).matches()) throw new BadRequestException("Nombre de " + que + " inválido: " + v);
        return s;
    }
}
