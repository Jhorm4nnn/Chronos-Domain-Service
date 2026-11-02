package com.sistematurnos.agenda.domain.exception;

public class TurnoNoEncontradoException extends NoEncontradoException {
    public TurnoNoEncontradoException(long id) {
        super("No existe el turno con id " + id);
    }
}
