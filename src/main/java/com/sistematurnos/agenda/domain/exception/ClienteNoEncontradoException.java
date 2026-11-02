package com.sistematurnos.agenda.domain.exception;

public class ClienteNoEncontradoException extends NoEncontradoException {
    public ClienteNoEncontradoException(long id) {
        super("No existe el cliente con id " + id);
    }
}
