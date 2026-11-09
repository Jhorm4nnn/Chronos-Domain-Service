package com.sistematurnos.agenda.infrastructure.adapters.out.persistence.mapper;

import com.sistematurnos.agenda.domain.model.Profesional;
import com.sistematurnos.agenda.domain.vo.Franja;
import com.sistematurnos.agenda.infrastructure.adapters.out.persistence.entity.FranjaEmbeddable;
import com.sistematurnos.agenda.infrastructure.adapters.out.persistence.entity.ProfesionalEntity;
import java.time.DayOfWeek;
import java.util.Locale;

public final class ProfesionalMapper {

    private ProfesionalMapper() { }

    public static Profesional aDominio(ProfesionalEntity e) {
        return Profesional.reconstruir(e.getId(), e.getNombre(),
                e.getDisponibilidad().stream()
                        .map(f -> new Franja(DayOfWeek.valueOf(f.getDia()), f.getDesde(), f.getHasta()))
                        .toList());
    }

    public static String clave(String nombre) {
        return nombre.trim().toLowerCase(Locale.ROOT);
    }

    public static void copiarA(ProfesionalEntity e, Profesional p) {
        e.setNombre(p.getNombre());
        e.setNombreClave(clave(p.getNombre()));
        e.getDisponibilidad().clear();
        p.getDisponibilidad().forEach(f -> e.getDisponibilidad().add(new FranjaEmbeddable(f.dia().name(), f.desde(), f.hasta())));
    }
}
