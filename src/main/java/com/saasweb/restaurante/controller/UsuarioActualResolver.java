package com.saasweb.restaurante.controller;

import com.saasweb.core.common.TenantContext;
import com.saasweb.core.repository.AdminUserRepository;
import com.saasweb.restaurante.model.Usuario;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Un parámetro {@link Usuario} en un controller del restaurante recibe a quien
 * está logueado (por su DNI de la sesión, dentro de la tienda actual).
 */
@Configuration
public class UsuarioActualResolver implements HandlerMethodArgumentResolver, WebMvcConfigurer {

    private final AdminUserRepository usuarios;

    public UsuarioActualResolver(AdminUserRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(this);
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType() == Usuario.class;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mav, NativeWebRequest request,
                                  WebDataBinderFactory binderFactory) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        String dni = auth.getName();
        return usuarios.findByDniForTenant(dni, TenantContext.getTenantId())
                .map(u -> new Usuario(u.getId(), u.getNombre(), u.getApellido()))
                // El superadmin no es usuario de la tienda: queda registrado por su DNI.
                .orElseGet(() -> new Usuario(dni, dni, null));
    }
}
