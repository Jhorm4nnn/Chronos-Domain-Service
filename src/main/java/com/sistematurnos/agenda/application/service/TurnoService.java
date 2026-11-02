package com.sistematurnos.agenda.application.service;

import com.sistematurnos.agenda.application.port.in.TurnoUseCase;
import com.sistematurnos.agenda.application.port.in.command.AgendarTurnoCommand;
import com.sistematurnos.agenda.application.port.in.command.ConsultarTurnosQuery;
import com.sistematurnos.agenda.application.port.in.command.ReprogramarTurnoCommand;
import com.sistematurnos.agenda.application.port.in.result.TurnoConDatos;
import com.sistematurnos.agenda.application.port.out.ClienteRepositoryPort;
import com.sistematurnos.agenda.application.port.out.FiltroTurno;
import com.sistematurnos.agenda.application.port.out.ProfesionalRepositoryPort;
import com.sistematurnos.agenda.application.port.out.TransaccionPort;
import com.sistematurnos.agenda.application.port.out.TurnoRepositoryPort;
import com.sistematurnos.agenda.domain.exception.*;
import com.sistematurnos.agenda.domain.model.Cliente;
import com.sistematurnos.agenda.domain.model.Profesional;
import com.sistematurnos.agenda.domain.model.Turno;
import com.sistematurnos.agenda.domain.service.ValidadorAgenda;
import com.sistematurnos.agenda.domain.vo.RangoTiempo;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TurnoService implements TurnoUseCase {

    private final ClienteRepositoryPort clientes;
    private final ProfesionalRepositoryPort profesionales;
    private final TurnoRepositoryPort turnos;
    private final TransaccionPort transaccion;
    private final Clock reloj;
    private final ValidadorAgenda validador;

    public TurnoService(ClienteRepositoryPort clientes, ProfesionalRepositoryPort profesionales, TurnoRepositoryPort turnos,
                        TransaccionPort transaccion, Clock reloj, ValidadorAgenda validador) {
        this.clientes = clientes;
        this.profesionales = profesionales;
        this.turnos = turnos;
        this.transaccion = transaccion;
        this.reloj = reloj;
        this.validador = validador;
    }

    @Override
    public TurnoConDatos agendar(AgendarTurnoCommand c) {
        List<String> faltantes = new ArrayList<>();
        if (c.clienteId() == null) faltantes.add("cliente");
        if (c.profesionalId() == null) faltantes.add("profesional");
        if (c.fecha() == null) faltantes.add("fecha");
        if (c.hora() == null) faltantes.add("hora");
        if (!faltantes.isEmpty()) {
            throw new ValorInvalidoException("Faltan campos obligatorios: " + String.join(", ", faltantes));
        }
        return transaccion.ejecutar(() -> {
            Cliente cliente = cargarCliente(c.clienteId());
            Profesional profesional = profesionales.buscarParaAgendar(c.profesionalId())
                    .orElseThrow(() -> new ProfesionalNoEncontradoException(c.profesionalId()));
            RangoTiempo rango = RangoTiempo.de(LocalDateTime.of(c.fecha(), c.hora()), c.duracionMinutos());
            validarHorario(profesional, rango, null);
            Turno guardado = turnos.guardar(Turno.agendar(cliente.getId(), profesional.getId(), rango));
            return new TurnoConDatos(guardado, cliente, profesional);
        });
    }

    @Override
    public TurnoConDatos cancelar(long turnoId) {
        return transaccion.ejecutar(() -> {
            Turno turno = cargarTurno(turnoId);
            turno.cancelar(ahora());
            return completar(turnos.guardar(turno));
        });
    }

    @Override
    public TurnoConDatos reprogramar(ReprogramarTurnoCommand c) {
        if (c.fecha() == null && c.hora() == null && c.duracionMinutos() == null) {
            throw new ValorInvalidoException("Indique la nueva fecha, la nueva hora o la nueva duración");
        }
        return transaccion.ejecutar(() -> {
            Turno turno = cargarTurno(c.turnoId());
            turno.asegurarModificable(ahora());   // primero el estado: un turno cancelado se explica solo
            Profesional profesional = profesionales.buscarParaAgendar(turno.getProfesionalId())
                    .orElseThrow(() -> new ProfesionalNoEncontradoException(turno.getProfesionalId()));
            LocalDate fecha = c.fecha() != null ? c.fecha() : turno.getRango().inicio().toLocalDate();
            LocalTime hora = c.hora() != null ? c.hora() : turno.getRango().inicio().toLocalTime();
            Integer minutos = c.duracionMinutos() != null ? c.duracionMinutos() : Integer.valueOf((int) turno.getRango().duracionMinutos());
            RangoTiempo nuevo = RangoTiempo.de(LocalDateTime.of(fecha, hora), minutos);
            validarHorario(profesional, nuevo, turno.getId());
            turno.reprogramar(nuevo, ahora());
            return completar(turnos.guardar(turno));
        });
    }

    @Override
    public TurnoConDatos porId(long turnoId) {
        return completar(cargarTurno(turnoId));
    }

    @Override
    public List<TurnoConDatos> consultar(ConsultarTurnosQuery q) {
        if (q.desde() != null && q.hasta() != null && q.desde().isAfter(q.hasta())) {
            throw new ValorInvalidoException("La fecha inicial no puede ser posterior a la final");
        }
        if (q.profesionalId() != null) {
            profesionales.buscarPorId(q.profesionalId()).orElseThrow(() -> new ProfesionalNoEncontradoException(q.profesionalId()));
        }
        if (q.clienteId() != null) {
            cargarCliente(q.clienteId());
        }
        List<Turno> encontrados = turnos.buscar(new FiltroTurno(q.profesionalId(), q.clienteId(), q.desde(), q.hasta(), q.incluirCancelados()));
        Set<Long> idsClientes = encontrados.stream().map(Turno::getClienteId).collect(Collectors.toSet());
        Set<Long> idsProfesionales = encontrados.stream().map(Turno::getProfesionalId).collect(Collectors.toSet());
        Map<Long, Cliente> mapaClientes = new HashMap<>();
        clientes.buscarPorIds(idsClientes).forEach(c -> mapaClientes.put(c.getId(), c));
        Map<Long, Profesional> mapaProfesionales = new HashMap<>();
        profesionales.buscarPorIds(idsProfesionales).forEach(p -> mapaProfesionales.put(p.getId(), p));
        return encontrados.stream()
                .map(t -> new TurnoConDatos(t, mapaClientes.get(t.getClienteId()), mapaProfesionales.get(t.getProfesionalId())))
                .toList();
    }

    /** Las tres reglas de agenda, en este orden: no pasado (400), dentro de la disponibilidad (409), sin cruces (409). */
    private void validarHorario(Profesional profesional, RangoTiempo rango, Long ignorarTurnoId) {
        validador.asegurarNoAnterior(rango, ahora());
        profesional.asegurarQueAtiende(rango);
        List<Turno> delDia = turnos.buscarAgendadosDelProfesional(profesional.getId(), rango.inicio().toLocalDate());
        validador.buscarConflicto(rango, delDia, ignorarTurnoId).ifPresent(conflicto -> {
            String nombre = clientes.buscarPorId(conflicto.getClienteId()).map(Cliente::getNombre).orElse("(sin datos)");
            throw new TurnoSolapadoException(conflicto, nombre);
        });
    }

    private LocalDateTime ahora() {
        return LocalDateTime.now(reloj);
    }

    private Cliente cargarCliente(long id) {
        return clientes.buscarPorId(id).orElseThrow(() -> new ClienteNoEncontradoException(id));
    }

    private Turno cargarTurno(long id) {
        return turnos.buscarPorId(id).orElseThrow(() -> new TurnoNoEncontradoException(id));
    }

    private TurnoConDatos completar(Turno t) {
        Cliente cliente = clientes.buscarPorId(t.getClienteId()).orElseThrow(() -> new ClienteNoEncontradoException(t.getClienteId()));
        Profesional profesional = profesionales.buscarPorId(t.getProfesionalId())
                .orElseThrow(() -> new ProfesionalNoEncontradoException(t.getProfesionalId()));
        return new TurnoConDatos(t, cliente, profesional);
    }
}
