package com.sistematurnos.agenda.domain.model;

import com.sistematurnos.agenda.domain.exception.ValorInvalidoException;
import com.sistematurnos.agenda.domain.vo.Telefono;

public class Cliente {

    private final Long id;
    private final String nombre;
    private final Telefono telefono;
    private final String correo;

    private Cliente(Long id, String nombre, Telefono telefono, String correo) {
        if (nombre == null || nombre.isBlank()) {
            throw new ValorInvalidoException("El nombre del cliente es obligatorio");
        }
        if (nombre.trim().length() > 120) {
            throw new ValorInvalidoException("El nombre no puede superar 120 caracteres");
        }
        if (telefono == null) {
            throw new ValorInvalidoException("El teléfono es obligatorio");
        }
        String correoLimpio = correo == null || correo.isBlank() ? null : correo.trim();
        if (correoLimpio != null && (!correoLimpio.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+") || correoLimpio.length() > 120)) {
            throw new ValorInvalidoException("El correo no tiene un formato válido");
        }
        this.id = id;
        this.nombre = nombre.trim();
        this.telefono = telefono;
        this.correo = correoLimpio;
    }

    /** Factory Method: un cliente nuevo nace sin id y válido. */
    public static Cliente registrar(String nombre, Telefono telefono, String correo) {
        return new Cliente(null, nombre, telefono, correo);
    }

    public static Cliente reconstruir(long id, String nombre, Telefono telefono, String correo) {
        return new Cliente(id, nombre, telefono, correo);
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public Telefono getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
}
