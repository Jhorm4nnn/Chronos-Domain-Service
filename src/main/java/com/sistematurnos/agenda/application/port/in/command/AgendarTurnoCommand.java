package com.sistematurnos.agenda.application.port.in.command;

import java.time.LocalDate;
import java.time.LocalTime;

/** Todos los campos pueden venir nulos desde afuera; el servicio responde cuáles faltan. duracionMinutos null = 30. */
public record AgendarTurnoCommand(Long clienteId, Long profesionalId, LocalDate fecha, LocalTime hora, Integer duracionMinutos) { }
