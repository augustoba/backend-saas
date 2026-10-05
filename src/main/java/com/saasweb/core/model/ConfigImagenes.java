package com.saasweb.core.model;

import jakarta.persistence.*;

/**
 * Cuenta del proveedor de imágenes de la tienda (hoy Cloudinary; el spec prevé
 * poder cambiarlo por Cloudflare R2). Una fila por tienda (id = tenant).
 */
@Entity
@Table(name = "core_config_imagenes")
public class ConfigImagenes {

    @Id
    private String id;

    /** Proveedor de las fotos (ver {@code ProveedorImagenes}); hoy sólo "CLOUDINARY". */
    @Column(nullable = false, length = 20)
    private String proveedor = "CLOUDINARY";

    @Column(length = 200)
    private String cloudinaryCloudName;

    @Column(length = 200)
    private String cloudinaryUploadPreset;

    /** Credenciales firmadas: sólo hacen falta para BORRAR fotos (el preset unsigned sólo sube). */
    @Column(length = 100)
    private String cloudinaryApiKey;

    @Column(length = 200)
    private String cloudinaryApiSecret;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }
    public String getCloudinaryCloudName() { return cloudinaryCloudName; }
    public void setCloudinaryCloudName(String cloudinaryCloudName) { this.cloudinaryCloudName = cloudinaryCloudName; }
    public String getCloudinaryUploadPreset() { return cloudinaryUploadPreset; }
    public void setCloudinaryUploadPreset(String cloudinaryUploadPreset) { this.cloudinaryUploadPreset = cloudinaryUploadPreset; }
    public String getCloudinaryApiKey() { return cloudinaryApiKey; }
    public void setCloudinaryApiKey(String cloudinaryApiKey) { this.cloudinaryApiKey = cloudinaryApiKey; }
    public String getCloudinaryApiSecret() { return cloudinaryApiSecret; }
    public void setCloudinaryApiSecret(String cloudinaryApiSecret) { this.cloudinaryApiSecret = cloudinaryApiSecret; }
}
