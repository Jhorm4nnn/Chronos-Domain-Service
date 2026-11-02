package com.sistematurnos.agenda.domain.exception;

public class ProfesionalNoEncontradoException extends NoEncontradoException {
    public ProfesionalNoEncontradoException(long id) {
        super("No existe el profesional con id " + id);
    }
}
