package com.saasweb;

import com.saasweb.core.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

// La auth es por JWT (ver SecurityConfig); no usamos el UserDetailsService por
// defecto de Spring Security (evita el "generated security password" en el log).
// Nombres de beans con el sistema delante para pos/restaurante (ver SistemaBeanNameGenerator).
// Los repositorios de Spring Data que se repiten llevan su nombre en @Repository.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class,
        nameGenerator = com.saasweb.core.config.SistemaBeanNameGenerator.class)
@EnableConfigurationProperties(AppProperties.class)
@EnableScheduling
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
