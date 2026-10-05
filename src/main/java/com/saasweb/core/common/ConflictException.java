package com.saasweb.core.common;

/** Choque con el estado actual (ej: una mesa que ya está ocupada). Responde 409. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
