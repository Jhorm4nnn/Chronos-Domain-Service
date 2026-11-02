package com.sistematurnos.agenda.infrastructure.adapters.in.rest.dto;

import java.util.List;

public record ProfesionalRequest(String nombre, List<FranjaDto> disponibilidad) { }
