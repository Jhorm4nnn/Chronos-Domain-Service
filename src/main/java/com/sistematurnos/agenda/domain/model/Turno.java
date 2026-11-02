package com.sistematurnos.agenda.domain.model;

import com.sistematurnos.agenda.domain.exception.TurnoEstadoInvalidoException;
import com.sistematurnos.agenda.domain.exception.ValorInvalidoException;
import com.sistematurnos.agenda.domain.vo.RangoTiempo;
import java.time.LocalDateTime;

public class Turno {

    private final Long id;
    private final long clienteId;
    private final long profesionalId;
    private RangoTiempo rango;
    private EstadoTurno estado;

    private Turno(Long id, long clienteId, long profesionalId, RangoTiempo rango, EstadoTurno estado) {
        if (rango == null || estado == null) {
            throw new ValorInvalidoException("El turno necesita horario y estado");
        }
        this.id = id;
        this.clienteId = clienteId;
        this.profesionalId = profesionalId;
        this.rango = rango;
        this.estado = estado;
    }

    /** Factory Method: un turno nuevo nace AGENDADO y sin id. */
    public static Turno agendar(long clienteId, long profesionalId, RangoTiempo rango) {
        return new Turno(null, clienteId, profesionalId, rango, EstadoTurno.AGENDADO);
    }

    public static Turno reconstruir(long id, long clienteId, long profesionalId, RangoTiempo rango, EstadoTurno estado) {
        return new Turno(id, clienteId, profesionalId, rango, estado);
    }

    /** Solo se modifica un turno vigente (no cancelado) que todavía no empezó. */
    public void asegurarModificable(LocalDateTime ahora) {
        if (estado == EstadoTurno.CANCELADO) {
            throw new TurnoEstadoInvalidoException("El turno " + id + " ya está cancelado");
        }
        if (!rango.inicio().isAfter(ahora)) {
            throw new TurnoEstadoInvalidoException("El turno " + id + " ya inició o pasó; no se puede modificar");
        }
    }

    public void cancelar(LocalDateTime ahora) {
        asegurarModificable(ahora);
        estado = EstadoTurno.CANCELADO;
    }

    public void reprogramar(RangoTiempo nuevoRango, LocalDateTime ahora) {
        asegurarModificable(ahora);
        this.rango = nuevoRango;
    }

    /** Solo un turno AGENDADO ocupa el horario. */
    public boolean bloquea(RangoTiempo otro) {
        return estado == EstadoTurno.AGENDADO && rango.seCruzaCon(otro);
    }

    public Long getId() { return id; }
    public long getClienteId() { return clienteId; }
    public long getProfesionalId() { return profesionalId; }
    public RangoTiempo getRango() { return rango; }
    public EstadoTurno getEstado() { return estado; }
}
