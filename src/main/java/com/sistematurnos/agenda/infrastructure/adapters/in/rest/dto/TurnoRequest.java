package com.sistematurnos.agenda.infrastructure.adapters.in.rest.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/** fecha: AAAA-MM-DD, hora: HH:mm. duracionMinutos es opcional (30 por defecto). */
public record TurnoRequest(Long clienteId, Long profesionalId, LocalDate fecha, LocalTime hora, Integer duracionMinutos) { }
