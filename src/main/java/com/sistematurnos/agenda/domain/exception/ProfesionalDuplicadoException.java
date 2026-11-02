package com.sistematurnos.agenda.domain.exception;

public class ProfesionalDuplicadoException extends DuplicadoException {
    public ProfesionalDuplicadoException(String nombre) {
        super("Ya existe un profesional registrado con el nombre " + nombre);
    }
}
