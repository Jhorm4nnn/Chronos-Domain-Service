package com.sistematurnos.agenda.domain.exception;

/** El horario pedido cae fuera de las franjas de disponibilidad del profesional. */
public class ProfesionalNoDisponibleException extends DominioException {
    public ProfesionalNoDisponibleException(String profesional, String detalle) {
        super("El profesional " + profesional + " no atiende en ese horario. " + detalle);
    }
}
