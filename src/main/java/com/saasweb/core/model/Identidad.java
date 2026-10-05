package com.saasweb.core.model;

import jakarta.persistence.*;

/**
 * Identidad del comercio: nombre, logo, color de marca, contacto y redes. La
 * usan los tres sistemas (sitio público, mails, comprobantes). Una fila por
 * tienda: el id es el del tenant (ver {@code ConfiguracionService}).
 */
@Entity
@Table(name = "core_identidad")
public class Identidad {

    @Id
    private String id;

    @Column(nullable = false)
    private String storeName;

    @Column(nullable = false)
    private String whatsappNumber;

    @Column(length = 2000)
    private String aboutText;

    private String instagram;

    private String facebookUrl;

    @Column(length = 5_000_000)
    private String logoUrl;

    /** "circle", "square" o "rectangle" (null = circle). */
    private String logoShape;

    /** Hex (#rrggbb) del color de marca; null = el del rubro. */
    private String brandColor;

    @Column(length = 500)
    private String storeAddress;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public String getWhatsappNumber() { return whatsappNumber; }
    public void setWhatsappNumber(String whatsappNumber) { this.whatsappNumber = whatsappNumber; }
    public String getAboutText() { return aboutText; }
    public void setAboutText(String aboutText) { this.aboutText = aboutText; }
    public String getInstagram() { return instagram; }
    public void setInstagram(String instagram) { this.instagram = instagram; }
    public String getFacebookUrl() { return facebookUrl; }
    public void setFacebookUrl(String facebookUrl) { this.facebookUrl = facebookUrl; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getLogoShape() { return logoShape; }
    public void setLogoShape(String logoShape) { this.logoShape = logoShape; }
    public String getBrandColor() { return brandColor; }
    public void setBrandColor(String brandColor) { this.brandColor = brandColor; }
    public String getStoreAddress() { return storeAddress; }
    public void setStoreAddress(String storeAddress) { this.storeAddress = storeAddress; }
}
