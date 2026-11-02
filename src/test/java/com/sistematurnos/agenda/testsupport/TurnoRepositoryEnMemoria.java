package com.sistematurnos.agenda.testsupport;

import com.sistematurnos.agenda.application.port.out.FiltroTurno;
import com.sistematurnos.agenda.application.port.out.TurnoRepositoryPort;
import com.sistematurnos.agenda.domain.model.EstadoTurno;
import com.sistematurnos.agenda.domain.model.Turno;
import java.time.LocalDate;
import java.util.*;

public class TurnoRepositoryEnMemoria implements TurnoRepositoryPort {

    private final Map<Long, Turno> filas = new TreeMap<>();
    private long siguiente = 1;

    private static Turno copia(long id, Turno t) {
        return Turno.reconstruir(id, t.getClienteId(), t.getProfesionalId(), t.getRango(), t.getEstado());
    }

    @Override
    public Turno guardar(Turno t) {
        long id = t.getId() == null ? siguiente++ : t.getId();
        filas.put(id, copia(id, t));
        return copia(id, t);
    }

    @Override
    public Optional<Turno> buscarPorId(long id) {
        Turno t = filas.get(id);
        return t == null ? Optional.empty() : Optional.of(copia(id, t));
    }

    @Override
    public List<Turno> buscarAgendadosDelProfesional(long profesionalId, LocalDate fecha) {
        return filas.values().stream()
                .filter(t -> t.getProfesionalId() == profesionalId && t.getEstado() == EstadoTurno.AGENDADO
                        && t.getRango().inicio().toLocalDate().equals(fecha))
                .map(t -> copia(t.getId(), t)).toList();
    }

    @Override
    public List<Turno> buscar(FiltroTurno f) {
        return filas.values().stream()
                .filter(t -> f.profesionalId() == null || t.getProfesionalId() == f.profesionalId())
                .filter(t -> f.clienteId() == null || t.getClienteId() == f.clienteId())
                .filter(t -> f.desde() == null || !t.getRango().inicio().toLocalDate().isBefore(f.desde()))
                .filter(t -> f.hasta() == null || !t.getRango().inicio().toLocalDate().isAfter(f.hasta()))
                .filter(t -> f.incluirCancelados() || t.getEstado() == EstadoTurno.AGENDADO)
                .sorted(Comparator.comparing((Turno t) -> t.getRango().inicio()).thenComparing(Turno::getId))
                .map(t -> copia(t.getId(), t)).toList();
    }

    public int cantidad() { return filas.size(); }
}
