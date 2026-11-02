package com.sistematurnos.agenda.infrastructure.adapters.in.rest.dto;

import com.sistematurnos.agenda.application.port.in.result.TurnoConDatos;
import java.time.LocalDate;
import java.time.LocalTime;

public record TurnoResponse(Long id, String estado, Long clienteId, String cliente, Long profesionalId, String profesional,
                            LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, long duracionMinutos) {

    public static TurnoResponse de(TurnoConDatos t) {
        var r = t.turno().getRango();
        return new TurnoResponse(t.turno().getId(), t.turno().getEstado().name(), t.cliente().getId(), t.cliente().getNombre(),
                t.profesional().getId(), t.profesional().getNombre(), r.inicio().toLocalDate(),
                r.inicio().toLocalTime(), r.fin().toLocalTime(), r.duracionMinutos());
    }
}
