package com.saasweb.core.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;

/**
 * Cada sistema tiene su propia lógica y puede repetir nombres de clase (el POS
 * y el restaurante tienen los dos su {@code CajaService}). Para que no choquen
 * los beans, lo de {@code pos} y {@code restaurante} lleva el sistema delante:
 * {@code posCajaService}, {@code restauranteCajaService}. El resto (núcleo y
 * ecommerce) conserva el nombre de siempre.
 */
public class SistemaBeanNameGenerator extends AnnotationBeanNameGenerator {

    @Override
    protected String buildDefaultBeanName(BeanDefinition definition) {
        String base = super.buildDefaultBeanName(definition);
        String clase = definition.getBeanClassName();
        if (clase == null) return base;
        for (String sistema : new String[]{"pos", "restaurante"}) {
            if (clase.startsWith("com.saasweb." + sistema + ".")) {
                return sistema + Character.toUpperCase(base.charAt(0)) + base.substring(1);
            }
        }
        return base;
    }
}
