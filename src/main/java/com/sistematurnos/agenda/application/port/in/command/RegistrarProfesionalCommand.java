package com.sistematurnos.agenda.application.port.in.command;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record RegistrarProfesionalCommand(String nombre, List<FranjaCommand> disponibilidad) {
    public record FranjaCommand(DayOfWeek dia, LocalTime desde, LocalTime hasta) { }
}
