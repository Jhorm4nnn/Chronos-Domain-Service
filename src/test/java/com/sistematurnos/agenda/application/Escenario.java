package com.sistematurnos.agenda.application;

import com.sistematurnos.agenda.application.port.in.command.*;
import com.sistematurnos.agenda.application.port.in.result.TurnoConDatos;
import com.sistematurnos.agenda.application.service.*;
import com.sistematurnos.agenda.domain.model.Cliente;
import com.sistematurnos.agenda.domain.model.Profesional;
import com.sistematurnos.agenda.domain.service.ValidadorAgenda;
import com.sistematurnos.agenda.testsupport.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Arma los servicios con dobles en memoria: así se prueba la aplicación sin Spring ni base de datos. */
final class Escenario {

    final ClienteRepositoryEnMemoria clientes = new ClienteRepositoryEnMemoria();
    final ProfesionalRepositoryEnMemoria profesionales = new ProfesionalRepositoryEnMemoria();
    final TurnoRepositoryEnMemoria turnos = new TurnoRepositoryEnMemoria();
    final TransaccionDirecta tx = new TransaccionDirecta();

    final ClienteService clienteService = new ClienteService(clientes);
    final ProfesionalService profesionalService = new ProfesionalService(profesionales);
    final TurnoService turnoService = new TurnoService(clientes, profesionales, turnos, tx, Datos.relojFijo(), new ValidadorAgenda());

    Cliente cliente(String nombre, String telefono) {
        return clienteService.registrar(new RegistrarClienteCommand(nombre, telefono, null));
    }

    /** Lunes a viernes 08:00-12:00 y 14:00-18:00. */
    Profesional doctora(String nombre) {
        List<RegistrarProfesionalCommand.FranjaCommand> franjas = new java.util.ArrayList<>();
        for (DayOfWeek d : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
            franjas.add(new RegistrarProfesionalCommand.FranjaCommand(d, LocalTime.of(8, 0), LocalTime.of(12, 0)));
            franjas.add(new RegistrarProfesionalCommand.FranjaCommand(d, LocalTime.of(14, 0), LocalTime.of(18, 0)));
        }
        return profesionalService.registrar(new RegistrarProfesionalCommand(nombre, franjas));
    }

    TurnoConDatos agendar(long clienteId, long profesionalId, LocalDate fecha, String hora, Integer minutos) {
        return turnoService.agendar(new AgendarTurnoCommand(clienteId, profesionalId, fecha, LocalTime.parse(hora), minutos));
    }
}
