package com.sistematurnos.agenda.infrastructure.adapters.in.rest.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

/** dia: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY o SUNDAY. Horas en formato HH:mm. */
public record FranjaDto(DayOfWeek dia, LocalTime desde, LocalTime hasta) { }
