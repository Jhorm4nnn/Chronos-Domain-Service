package com.sistematurnos.agenda.application.port.out;

import com.sistematurnos.agenda.domain.model.Turno;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TurnoRepositoryPort {
    Turno guardar(Turno turno);
    Optional<Turno> buscarPorId(long id);
    /** Los turnos AGENDADOS de ese profesional que empiezan en esa fecha. La regla de cruce la decide el dominio. */
    List<Turno> buscarAgendadosDelProfesional(long profesionalId, LocalDate fecha);
    /** Ordenados por inicio ascendente (y por id si empatan). */
    List<Turno> buscar(FiltroTurno filtro);
}
