package com.sistematurnos.agenda.domain.vo;

import com.sistematurnos.agenda.domain.exception.ValorInvalidoException;

/** Teléfono normalizado: solo dígitos (se aceptan espacios, guiones y un "+" inicial), entre 7 y 15. */
public record Telefono(String valor) {

    public Telefono {
        if (valor == null || !valor.matches("[0-9]{7,15}")) {
            throw new ValorInvalidoException("El teléfono debe tener entre 7 y 15 dígitos");
        }
    }

    public static Telefono de(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new ValorInvalidoException("El teléfono es obligatorio");
        }
        return new Telefono(texto.replaceAll("[\\s\\-+()]", ""));
    }
}
