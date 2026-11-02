package com.sistematurnos.agenda.domain.exception;

import com.sistematurnos.agenda.domain.model.Turno;

/** El profesional ya tiene otro turno agendado que se cruza con el pedido. Lleva el turno en conflicto para explicarlo. */
public class TurnoSolapadoException extends DominioException {

    private final Turno conflicto;

    public TurnoSolapadoException(Turno conflicto, String nombreCliente) {
        super("El horario se cruza con el turno " + conflicto.getId() + " del cliente " + nombreCliente
                + " (" + conflicto.getRango().descripcion() + ")");
        this.conflicto = conflicto;
    }

    public Turno getConflicto() {
        return conflicto;
    }
}
