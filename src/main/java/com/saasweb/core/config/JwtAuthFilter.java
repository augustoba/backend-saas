package com.saasweb.core.config;

import com.saasweb.core.common.TenantContext;
import com.saasweb.core.model.AdminUser;
import com.saasweb.core.service.PermisoService;
import com.saasweb.core.repository.AdminUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lee `Authorization: Bearer <jwt>` y, si es válido, autentica al usuario con
 * sus permisos actuales (los busca en la base cada request, así un cambio de
 * rol tiene efecto inmediato). Authorities = ROLE_ADMIN + un authority por
 * cada permiso del rol (ver {@link PermisoService}).
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AdminUserRepository users;
    private final PermisoService permisos;

    public JwtAuthFilter(JwtService jwtService, AdminUserRepository users, PermisoService permisos) {
        this.jwtService = jwtService;
        this.users = users;
        this.permisos = permisos;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String dni = jwtService.validate(header.substring(7));
            if (dni != null) {
                AdminUser user = users.findByDniForTenant(dni, TenantContext.getTenantId())
                        .filter(AdminUser::isEnabled).orElse(null);
                if (user != null) {
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    for (String p : user.isSystemAdmin() ? permisos.todas() : user.permissions()) {
                        authorities.add(new SimpleGrantedAuthority(p));
                    }
                    if (user.isSuperAdmin()) {
                        authorities.add(new SimpleGrantedAuthority("SUPERADMIN"));
                    }
                    var auth = new UsernamePasswordAuthenticationToken(dni, null, authorities);
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        chain.doFilter(request, response);
    }
}
