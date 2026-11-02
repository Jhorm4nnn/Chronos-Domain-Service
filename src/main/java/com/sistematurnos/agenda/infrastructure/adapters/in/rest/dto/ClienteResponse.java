package com.sistematurnos.agenda.infrastructure.adapters.in.rest.dto;

import com.sistematurnos.agenda.domain.model.Cliente;

public record ClienteResponse(Long id, String nombre, String telefono, String correo) {
    public static ClienteResponse de(Cliente c) {
        return new ClienteResponse(c.getId(), c.getNombre(), c.getTelefono().valor(), c.getCorreo());
    }
}
