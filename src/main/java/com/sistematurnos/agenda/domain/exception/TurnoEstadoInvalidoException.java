package com.sistematurnos.agenda.domain.exception;

/** La operación no se puede hacer en el estado actual del turno (ya cancelado o ya iniciado). */
public class TurnoEstadoInvalidoException extends DominioException {
    public TurnoEstadoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
