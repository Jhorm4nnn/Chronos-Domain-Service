package com.sistematurnos.agenda.infrastructure.adapters.in.rest.dto;

import com.sistematurnos.agenda.domain.model.Profesional;
import java.util.List;

public record ProfesionalResponse(Long id, String nombre, List<FranjaDto> disponibilidad) {
    public static ProfesionalResponse de(Profesional p) {
        return new ProfesionalResponse(p.getId(), p.getNombre(),
                p.getDisponibilidad().stream().map(f -> new FranjaDto(f.dia(), f.desde(), f.hasta())).toList());
    }
}
