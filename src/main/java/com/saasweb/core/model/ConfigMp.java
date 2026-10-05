package com.saasweb.core.model;

import jakarta.persistence.*;

/**
 * Cuenta de Mercado Pago de la tienda: no es compartida, cada una cobra en la
 * suya. Una fila por tienda (id = tenant).
 */
@Entity
@Table(name = "core_config_mp")
public class ConfigMp {

    @Id
    private String id;

    @Column(nullable = false)
    private boolean mpEnabled = false;

    @Column(length = 300)
    private String mpAccessToken;

    @Column(length = 300)
    private String mpPublicKey;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public boolean isMpEnabled() { return mpEnabled; }
    public void setMpEnabled(boolean mpEnabled) { this.mpEnabled = mpEnabled; }
    public String getMpAccessToken() { return mpAccessToken; }
    public void setMpAccessToken(String mpAccessToken) { this.mpAccessToken = mpAccessToken; }
    public String getMpPublicKey() { return mpPublicKey; }
    public void setMpPublicKey(String mpPublicKey) { this.mpPublicKey = mpPublicKey; }

    /** Habilitado y con el Access Token cargado. */
    public boolean listo() {
        return mpEnabled && mpAccessToken != null && !mpAccessToken.isBlank();
    }
}
