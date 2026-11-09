package com.sistematurnos.agenda.application.port.out;

import java.time.LocalDate;

/** Cualquier campo nulo significa «sin filtrar por eso». desde y hasta son inclusivos. */
public record FiltroTurno(Long profesionalId, Long clienteId, LocalDate desde, LocalDate hasta, boolean incluirCancelados) { }
