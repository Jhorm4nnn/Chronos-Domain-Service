package com.sistematurnos.agenda.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.sistematurnos.agenda.domain.exception.*;
import com.sistematurnos.agenda.domain.model.*;
import com.sistematurnos.agenda.domain.service.ValidadorAgenda;
import com.sistematurnos.agenda.domain.vo.Franja;
import com.sistematurnos.agenda.domain.vo.Telefono;
import com.sistematurnos.agenda.testsupport.Datos;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ModeloTest {

    @Test
    void clienteValida() {
        Cliente c = Cliente.registrar("  Ana Pérez ", Telefono.de("3001234567"), " ");
        assertEquals("Ana Pérez", c.getNombre());
        assertNull(c.getCorreo());
        assertThrows(ValorInvalidoException.class, () -> Cliente.registrar(" ", Telefono.de("3001234567"), null));
        assertThrows(ValorInvalidoException.class, () -> Cliente.registrar("Ana", Telefono.de("3001234567"), "no-es-correo"));
        assertThrows(ValorInvalidoException.class, () -> Cliente.registrar("Ana", null, null));
    }

    @Test
    void profesionalNecesitaFranjasSinTraslape() {
        assertThrows(ValorInvalidoException.class, () -> Profesional.registrar("Dr. X", List.of()));
        assertThrows(ValorInvalidoException.class, () -> Profesional.registrar(" ", Datos.semanaLaboral()));
        assertThrows(ValorInvalidoException.class, () -> Profesional.registrar("Dr. X", List.of(
                new Franja(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0)),
                new Franja(DayOfWeek.MONDAY, LocalTime.of(11, 0), LocalTime.of(15, 0)))));
        Profesional ok = Profesional.registrar("Dr. X", List.of(
                new Franja(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0)),
                new Franja(DayOfWeek.MONDAY, LocalTime.of(12, 0), LocalTime.of(15, 0))));   // contiguas: permitido
        assertEquals(2, ok.getDisponibilidad().size());
    }

    @Test
    @DisplayName("El profesional atiende solo dentro de sus franjas")
    void disponibilidad() {
        Profesional p = Datos.doctora();
        assertTrue(p.atiende(Datos.rango(Datos.MANANA, "09:00", 30)));
        assertFalse(p.atiende(Datos.rango(Datos.MANANA, "12:00", 30)));   // hora de almuerzo
        assertFalse(p.atiende(Datos.rango(Datos.SABADO, "09:00", 30)));   // no trabaja sábados
        assertThrows(ProfesionalNoDisponibleException.class, () -> p.asegurarQueAtiende(Datos.rango(Datos.SABADO, "09:00", 30)));
    }

    @Test
    void turnoNaceAgendadoYSeCancela() {
        Turno t = Turno.agendar(1, 2, Datos.rango(Datos.MANANA, "09:00", 30));
        assertEquals(EstadoTurno.AGENDADO, t.getEstado());
        assertNull(t.getId());
        t.cancelar(Datos.AHORA);
        assertEquals(EstadoTurno.CANCELADO, t.getEstado());
    }

    @Test
    void noSeCancelaDosVecesNiUnTurnoQueYaEmpezo() {
        Turno t = Turno.reconstruir(1, 1, 2, Datos.rango(Datos.MANANA, "09:00", 30), EstadoTurno.CANCELADO);
        assertThrows(TurnoEstadoInvalidoException.class, () -> t.cancelar(Datos.AHORA));
        Turno pasado = Turno.reconstruir(2, 1, 2, Datos.rango(Datos.HOY, "09:00", 30), EstadoTurno.AGENDADO);
        assertThrows(TurnoEstadoInvalidoException.class, () -> pasado.cancelar(Datos.AHORA));
        assertThrows(TurnoEstadoInvalidoException.class, () -> pasado.reprogramar(Datos.rango(Datos.MANANA, "09:00", 30), Datos.AHORA));
    }

    @Test
    void reprogramarCambiaElRango() {
        Turno t = Turno.reconstruir(1, 1, 2, Datos.rango(Datos.MANANA, "09:00", 30), EstadoTurno.AGENDADO);
        t.reprogramar(Datos.rango(Datos.MANANA, "10:00", 45), Datos.AHORA);
        assertEquals(Datos.en(Datos.MANANA, "10:45"), t.getRango().fin());
    }

    @Test
    @DisplayName("Un turno cancelado no bloquea el horario")
    void soloAgendadoBloquea() {
        Turno vigente = Turno.reconstruir(1, 1, 2, Datos.rango(Datos.MANANA, "09:00", 30), EstadoTurno.AGENDADO);
        Turno cancelado = Turno.reconstruir(2, 1, 2, Datos.rango(Datos.MANANA, "09:00", 30), EstadoTurno.CANCELADO);
        assertTrue(vigente.bloquea(Datos.rango(Datos.MANANA, "09:10", 30)));
        assertFalse(cancelado.bloquea(Datos.rango(Datos.MANANA, "09:10", 30)));
    }

    @Test
    void validadorEncuentraConflictoIgnorandoElPropioTurno() {
        ValidadorAgenda v = new ValidadorAgenda();
        Turno a = Turno.reconstruir(1, 1, 2, Datos.rango(Datos.MANANA, "09:00", 30), EstadoTurno.AGENDADO);
        Turno b = Turno.reconstruir(2, 1, 2, Datos.rango(Datos.MANANA, "10:00", 30), EstadoTurno.AGENDADO);
        assertEquals(1L, v.buscarConflicto(Datos.rango(Datos.MANANA, "09:15", 30), List.of(a, b), null).orElseThrow().getId().longValue());
        assertTrue(v.buscarConflicto(Datos.rango(Datos.MANANA, "09:30", 30), List.of(a, b), null).isEmpty());
        assertTrue(v.buscarConflicto(Datos.rango(Datos.MANANA, "09:15", 30), List.of(a, b), 1L).isEmpty());
    }

    @Test
    void validadorRechazaElPasado() {
        ValidadorAgenda v = new ValidadorAgenda();
        assertThrows(ValorInvalidoException.class, () -> v.asegurarNoAnterior(Datos.rango(Datos.HOY, "09:00", 30), Datos.AHORA));
        v.asegurarNoAnterior(Datos.rango(Datos.HOY, "10:00", 30), Datos.AHORA);   // justo ahora: permitido
    }
}
