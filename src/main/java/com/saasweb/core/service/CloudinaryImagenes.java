package com.saasweb.core.service;

import com.saasweb.core.model.ConfigImagenes;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cloudinary con un "unsigned upload preset": el navegador sube directo sin
 * exponer el API secret. La URL resultante viene en {@code secure_url}. Borrar
 * sí necesita la API Key y el Secret (request firmada, portado del ecommerce de Ruth).
 */
@Component
public class CloudinaryImagenes implements ProveedorImagenes {

    public static final String CLAVE = "CLOUDINARY";

    private static final Logger log = LoggerFactory.getLogger(CloudinaryImagenes.class);

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

    @Override
    public void borrar(ConfigImagenes cfg, List<String> urls) {
        if (urls == null || urls.isEmpty()) return;
        String cloudName = cfg.getCloudinaryCloudName();
        String apiKey = cfg.getCloudinaryApiKey();
        String apiSecret = cfg.getCloudinaryApiSecret();
        if (!notBlank(cloudName) || !notBlank(apiKey) || !notBlank(apiSecret)) {
            log.info("Cloudinary sin API Key/Secret: no se borran {} imagen(es) de la cuenta.", urls.size());
            return;
        }
        for (String url : urls) {
            try {
                String publicId = publicIdDe(url);
                if (publicId == null) continue;
                long timestamp = Instant.now().getEpochSecond();
                MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
                form.add("public_id", publicId);
                form.add("api_key", apiKey);
                form.add("timestamp", String.valueOf(timestamp));
                form.add("signature", sha1Hex("public_id=" + publicId + "&timestamp=" + timestamp + apiSecret));
                RestClient.create().post()
                        .uri("https://api.cloudinary.com/v1_1/" + cloudName + "/image/destroy")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(form).retrieve().toBodilessEntity();
            } catch (Exception e) {
                log.warn("No se pudo borrar de Cloudinary la imagen {}: {}", url, e.getMessage());
            }
        }
    }

    /**
     * De {@code https://res.cloudinary.com/<cloud>/image/upload/v169.../carpeta/nombre.jpg}
     * saca el public_id ({@code carpeta/nombre}). null si no es una URL de Cloudinary.
     */
    static String publicIdDe(String url) {
        if (url == null) return null;
        String marker = "/image/upload/";
        int at = url.indexOf(marker);
        if (at == -1 || !url.contains(".cloudinary.com/")) return null;
        String after = url.substring(at + marker.length());
        int slash = after.indexOf("/");
        String rest = (slash != -1 && after.substring(0, slash).matches("v\\d+")) ? after.substring(slash + 1) : after;
        int dot = rest.lastIndexOf(".");
        return dot > 0 ? rest.substring(0, dot) : rest;
    }

    private static String sha1Hex(String input) throws NoSuchAlgorithmException {
        byte[] hash = MessageDigest.getInstance("SHA-1").digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private static boolean notBlank(String v) {
        return v != null && !v.isBlank();
    }
}
