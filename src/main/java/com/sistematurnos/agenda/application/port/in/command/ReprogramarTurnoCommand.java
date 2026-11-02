package com.sistematurnos.agenda.application.port.in.command;

import java.time.LocalDate;
import java.time.LocalTime;

/** Lo que viene nulo se conserva: se puede cambiar solo la fecha, solo la hora, solo la duración, o varias. */
public record ReprogramarTurnoCommand(long turnoId, LocalDate fecha, LocalTime hora, Integer duracionMinutos) { }
