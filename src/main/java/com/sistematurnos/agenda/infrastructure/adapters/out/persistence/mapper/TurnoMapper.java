package com.sistematurnos.agenda.infrastructure.adapters.out.persistence.mapper;

import com.sistematurnos.agenda.domain.model.EstadoTurno;
import com.sistematurnos.agenda.domain.model.Turno;
import com.sistematurnos.agenda.domain.vo.RangoTiempo;
import com.sistematurnos.agenda.infrastructure.adapters.out.persistence.entity.TurnoEntity;

public final class TurnoMapper {

    private TurnoMapper() { }

    public static Turno aDominio(TurnoEntity e) {
        return Turno.reconstruir(e.getId(), e.getClienteId(), e.getProfesionalId(),
                new RangoTiempo(e.getInicio(), e.getFin()), EstadoTurno.valueOf(e.getEstado()));
    }

    public static void copiarA(TurnoEntity e, Turno t) {
        e.setClienteId(t.getClienteId());
        e.setProfesionalId(t.getProfesionalId());
        e.setInicio(t.getRango().inicio());
        e.setFin(t.getRango().fin());
        e.setEstado(t.getEstado().name());
    }
}
