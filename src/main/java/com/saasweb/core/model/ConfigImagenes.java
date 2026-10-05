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

    @Column(length = 200)
    private String cloudinaryCloudName;

    @Column(length = 200)
    private String cloudinaryUploadPreset;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCloudinaryCloudName() { return cloudinaryCloudName; }
    public void setCloudinaryCloudName(String cloudinaryCloudName) { this.cloudinaryCloudName = cloudinaryCloudName; }
    public String getCloudinaryUploadPreset() { return cloudinaryUploadPreset; }
    public void setCloudinaryUploadPreset(String cloudinaryUploadPreset) { this.cloudinaryUploadPreset = cloudinaryUploadPreset; }
}
