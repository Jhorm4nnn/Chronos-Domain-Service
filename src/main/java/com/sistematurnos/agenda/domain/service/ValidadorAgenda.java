package com.sistematurnos.agenda.domain.service;

import com.sistematurnos.agenda.domain.exception.ValorInvalidoException;
import com.sistematurnos.agenda.domain.model.Turno;
import com.sistematurnos.agenda.domain.vo.RangoTiempo;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Reglas de agenda que no pertenecen a una sola entidad: no pasado y no cruce con otros turnos del profesional. */
public class ValidadorAgenda {

    public void asegurarNoAnterior(RangoTiempo rango, LocalDateTime ahora) {
        if (rango.inicio().isBefore(ahora)) {
            throw new ValorInvalidoException("La fecha y hora del turno no pueden ser anteriores a la actual");
        }
    }

    /** El primer turno vigente que se cruza con el rango pedido, ignorando (si se indica) el turno que se está reprogramando. */
    public Optional<Turno> buscarConflicto(RangoTiempo rango, List<Turno> turnosDelProfesional, Long ignorarTurnoId) {
        return turnosDelProfesional.stream()
                .filter(t -> ignorarTurnoId == null || !ignorarTurnoId.equals(t.getId()))
                .filter(t -> t.bloquea(rango))
                .findFirst();
    }
}
