package com.sistematurnos.agenda.application.port.in.result;

import com.sistematurnos.agenda.domain.model.Cliente;
import com.sistematurnos.agenda.domain.model.Profesional;
import com.sistematurnos.agenda.domain.model.Turno;

/** Un turno junto con el cliente y el profesional a los que se refiere, para mostrarlo completo. */
public record TurnoConDatos(Turno turno, Cliente cliente, Profesional profesional) { }
