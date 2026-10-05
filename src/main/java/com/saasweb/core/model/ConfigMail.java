package com.saasweb.core.model;

import jakarta.persistence.*;

/**
 * SMTP propio de la tienda. Vacío = se usa el servicio de mail de la
 * plataforma. Una fila por tienda (id = tenant).
 */
@Entity
@Table(name = "core_config_mail")
public class ConfigMail {

    @Id
    private String id;

    @Column(length = 300)
    private String smtpHost;

    private Integer smtpPort;

    @Column(length = 300)
    private String smtpUsername;

    @Column(length = 500)
    private String smtpPassword;

    @Column(length = 300)
    private String smtpFromEmail;

    @Column(length = 200)
    private String smtpFromName;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSmtpHost() { return smtpHost; }
    public void setSmtpHost(String smtpHost) { this.smtpHost = smtpHost; }
    public Integer getSmtpPort() { return smtpPort; }
    public void setSmtpPort(Integer smtpPort) { this.smtpPort = smtpPort; }
    public String getSmtpUsername() { return smtpUsername; }
    public void setSmtpUsername(String smtpUsername) { this.smtpUsername = smtpUsername; }
    public String getSmtpPassword() { return smtpPassword; }
    public void setSmtpPassword(String smtpPassword) { this.smtpPassword = smtpPassword; }
    public String getSmtpFromEmail() { return smtpFromEmail; }
    public void setSmtpFromEmail(String smtpFromEmail) { this.smtpFromEmail = smtpFromEmail; }
    public String getSmtpFromName() { return smtpFromName; }
    public void setSmtpFromName(String smtpFromName) { this.smtpFromName = smtpFromName; }
}
