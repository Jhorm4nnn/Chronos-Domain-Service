package com.sistematurnos.agenda.application.port.in.command;

import java.time.LocalDate;

public record ConsultarTurnosQuery(Long profesionalId, Long clienteId, LocalDate desde, LocalDate hasta, boolean incluirCancelados) { }
