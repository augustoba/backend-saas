package com.saasweb.core.dto;

import jakarta.validation.constraints.Size;

public final class ImagenDtos {

    private ImagenDtos() {
    }

    /**
     * Pedido de ticket para subir una imagen. {@code carpeta} es relativa a la
     * tienda; {@code slugTienda} sólo lo acepta el superadmin (ver
     * {@code ImagenService#ticket}).
     */
    public record SubidaRequest(@Size(max = 200) String carpeta, @Size(max = 80) String nombre,
                                @Size(max = 80) String slugTienda) {
    }
}
