package com.sistematurnos.agenda.domain.exception;

public class ClienteDuplicadoException extends DuplicadoException {
    public ClienteDuplicadoException(String telefono) {
        super("Ya existe un cliente registrado con el teléfono " + telefono);
    }
}
