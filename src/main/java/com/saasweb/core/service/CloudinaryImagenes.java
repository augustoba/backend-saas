package com.saasweb.core.service;

import com.saasweb.core.model.ConfigImagenes;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cloudinary con un "unsigned upload preset": el navegador sube directo sin
 * exponer el API secret. La URL resultante viene en {@code secure_url}.
 */
@Component
public class CloudinaryImagenes implements ProveedorImagenes {

    public static final String CLAVE = "CLOUDINARY";

    @Override
    public String clave() {
        return CLAVE;
    }

    @Override
    public boolean configurado(ConfigImagenes cfg) {
        return notBlank(cfg.getCloudinaryCloudName()) && notBlank(cfg.getCloudinaryUploadPreset());
    }

    @Override
    public TicketSubida ticket(ConfigImagenes cfg, String carpeta, String nombre) {
        Map<String, String> campos = new LinkedHashMap<>();
        campos.put("upload_preset", cfg.getCloudinaryUploadPreset());
        campos.put("folder", carpeta);
        if (nombre != null) campos.put("public_id", nombre);
        return new TicketSubida(CLAVE, "POST_FORM",
                "https://api.cloudinary.com/v1_1/" + cfg.getCloudinaryCloudName() + "/image/upload",
                campos, "file", "secure_url", null);
    }

    private static boolean notBlank(String v) {
        return v != null && !v.isBlank();
    }
}
