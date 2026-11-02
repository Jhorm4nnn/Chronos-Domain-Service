package com.sistematurnos.agenda.infrastructure.adapters.in.rest.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/** Lo que no se envíe se conserva. Debe venir al menos uno de los tres. */
public record ReprogramacionRequest(LocalDate fecha, LocalTime hora, Integer duracionMinutos) { }
