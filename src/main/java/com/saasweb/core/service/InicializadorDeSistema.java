package com.saasweb.core.service;

import com.saasweb.core.dto.TenantAdminDtos.TenantCreateRequest;
import com.saasweb.core.model.Sistema;
import com.saasweb.core.model.Tenant;

/**
 * Lo que cada sistema siembra en una tienda recién creada (su configuración,
 * datos de ejemplo, etc.). El núcleo crea la tienda, sus roles, su admin y su
 * identidad, y después llama al inicializador de su sistema — así el alta no
 * conoce productos ni mesas (ver el spec de la reestructuración).
 */
public interface InicializadorDeSistema {

    /** true si atiende tiendas de ese sistema. */
    boolean aplicaA(Sistema sistema);

    void inicializar(Tenant tenant, TenantCreateRequest req);
}
