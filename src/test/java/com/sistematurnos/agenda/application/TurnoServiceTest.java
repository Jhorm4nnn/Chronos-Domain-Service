package com.sistematurnos.agenda.application;

import static com.sistematurnos.agenda.testsupport.Datos.*;
import static org.junit.jupiter.api.Assertions.*;

import com.sistematurnos.agenda.application.port.in.command.*;
import com.sistematurnos.agenda.application.port.in.result.TurnoConDatos;
import com.sistematurnos.agenda.domain.exception.*;
import com.sistematurnos.agenda.domain.model.EstadoTurno;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TurnoServiceTest {

    private final Escenario e = new Escenario();
    private final long ana = e.cliente("Ana Pérez", "3001234567").getId();
    private final long luis = e.cliente("Luis Mora", "3109999999").getId();
    private final long lopez = e.doctora("Dra. López").getId();
    private final long gomez = e.doctora("Dr. Gómez").getId();

    // ---------------- HU-9: crear ----------------

    @Test
    @DisplayName("Crear deja el turno AGENDADO con los datos completos y 30 minutos por defecto")
    void agenda() {
        TurnoConDatos t = e.agendar(ana, lopez, MANANA, "09:00", null);
        assertNotNull(t.turno().getId());
        assertEquals(EstadoTurno.AGENDADO, t.turno().getEstado());
        assertEquals(en(MANANA, "09:30"), t.turno().getRango().fin());
        assertEquals("Ana Pérez", t.cliente().getNombre());
        assertEquals("Dra. López", t.profesional().getNombre());
    }

    @Test
    void listaTodosLosCamposFaltantes() {
        ValorInvalidoException ex = assertThrows(ValorInvalidoException.class,
                () -> e.turnoService.agendar(new AgendarTurnoCommand(null, null, null, LocalTime.of(9, 0), null)));
        assertTrue(ex.getMessage().contains("cliente"));
        assertTrue(ex.getMessage().contains("profesional"));
        assertTrue(ex.getMessage().contains("fecha"));
        assertEquals(0, e.turnos.cantidad());
    }

    @Test
    void clienteOProfesionalInexistentes() {
        assertThrows(ClienteNoEncontradoException.class, () -> e.agendar(99, lopez, MANANA, "09:00", null));
        assertThrows(ProfesionalNoEncontradoException.class, () -> e.agendar(ana, 99, MANANA, "09:00", null));
    }

    @Test
    @DisplayName("No se agenda en el pasado, pero sí justo ahora")
    void noPasado() {
        assertThrows(ValorInvalidoException.class, () -> e.agendar(ana, lopez, HOY, "09:00", null));
        e.agendar(ana, lopez, HOY, "10:00", null);
    }

    @Test
    void duracionInvalida() {
        assertThrows(ValorInvalidoException.class, () -> e.agendar(ana, lopez, MANANA, "09:00", 2));
        assertThrows(ValorInvalidoException.class, () -> e.agendar(ana, lopez, MANANA, "09:00", 600));
        assertEquals(0, e.turnos.cantidad());
    }

    @Test
    @DisplayName("Fuera de la disponibilidad del profesional: sábado, almuerzo o pasándose del final de la franja")
    void fueraDeDisponibilidad() {
        assertThrows(ProfesionalNoDisponibleException.class, () -> e.agendar(ana, lopez, SABADO, "09:00", null));
        assertThrows(ProfesionalNoDisponibleException.class, () -> e.agendar(ana, lopez, MANANA, "12:00", null));
        assertThrows(ProfesionalNoDisponibleException.class, () -> e.agendar(ana, lopez, MANANA, "11:45", 30));
        e.agendar(ana, lopez, MANANA, "11:30", 30);   // termina justo a las 12:00
        assertEquals(1, e.turnos.cantidad());
    }

    // ---------------- HU-10: no solapamiento ----------------

    @Test
    @DisplayName("Un cruce se rechaza y el mensaje dice con qué turno, de qué cliente y cuándo")
    void rechazaCruces() {
        long primero = e.agendar(ana, lopez, MANANA, "10:00", 30).turno().getId();
        TurnoSolapadoException ex = assertThrows(TurnoSolapadoException.class, () -> e.agendar(luis, lopez, MANANA, "10:15", 30));
        assertEquals(primero, ex.getConflicto().getId().longValue());
        assertTrue(ex.getMessage().contains("Ana Pérez"));
        assertTrue(ex.getMessage().contains("10:00"));
        assertEquals(1, e.turnos.cantidad());
    }

    @Test
    void turnosConsecutivosNoSeCruzan() {
        e.agendar(ana, lopez, MANANA, "10:00", 30);
        e.agendar(luis, lopez, MANANA, "10:30", 30);
        e.agendar(luis, lopez, MANANA, "09:30", 30);
        assertEquals(3, e.turnos.cantidad());
    }

    @Test
    @DisplayName("La validación no depende del cliente: ni el mismo cliente puede repetirse en el rango")
    void mismoClienteTambienSeRechaza() {
        e.agendar(ana, lopez, MANANA, "10:00", 30);
        assertThrows(TurnoSolapadoException.class, () -> e.agendar(ana, lopez, MANANA, "10:10", 30));
    }

    @Test
    void otroProfesionalNoCuenta() {
        e.agendar(ana, lopez, MANANA, "10:00", 30);
        e.agendar(luis, gomez, MANANA, "10:00", 30);
        assertEquals(2, e.turnos.cantidad());
    }

    @Test
    @DisplayName("Un turno cancelado libera el horario")
    void canceladoNoBloquea() {
        long id = e.agendar(ana, lopez, MANANA, "10:00", 30).turno().getId();
        e.turnoService.cancelar(id);
        e.agendar(luis, lopez, MANANA, "10:00", 30);
    }

    // ---------------- HU-11: cancelar y reprogramar ----------------

    @Test
    void cancelaYConservaElHistorial() {
        long id = e.agendar(ana, lopez, MANANA, "10:00", 30).turno().getId();
        TurnoConDatos c = e.turnoService.cancelar(id);
        assertEquals(EstadoTurno.CANCELADO, c.turno().getEstado());
        assertEquals(EstadoTurno.CANCELADO, e.turnoService.porId(id).turno().getEstado());
        assertEquals(1, e.turnos.cantidad());
    }

    @Test
    void cancelarInexistenteODosVeces() {
        assertThrows(TurnoNoEncontradoException.class, () -> e.turnoService.cancelar(99));
        long id = e.agendar(ana, lopez, MANANA, "10:00", 30).turno().getId();
        e.turnoService.cancelar(id);
        assertThrows(TurnoEstadoInvalidoException.class, () -> e.turnoService.cancelar(id));
        assertThrows(TurnoEstadoInvalidoException.class,
                () -> e.turnoService.reprogramar(new ReprogramarTurnoCommand(id, MANANA, LocalTime.of(11, 0), null)));
    }

    @Test
    void reprograma() {
        long id = e.agendar(ana, lopez, MANANA, "10:00", 30).turno().getId();
        TurnoConDatos r = e.turnoService.reprogramar(new ReprogramarTurnoCommand(id, MANANA.plusDays(1), LocalTime.of(15, 0), 45));
        assertEquals(en(MANANA.plusDays(1), "15:00"), r.turno().getRango().inicio());
        assertEquals(en(MANANA.plusDays(1), "15:45"), r.turno().getRango().fin());
        assertEquals("Ana Pérez", r.cliente().getNombre());
        e.agendar(luis, lopez, MANANA, "10:00", 30);   // el horario anterior quedó libre
    }

    @Test
    @DisplayName("Reprogramar solo la hora conserva la fecha y la duración")
    void reprogramarSoloLaHora() {
        long id = e.agendar(ana, lopez, MANANA, "10:00", 45).turno().getId();
        TurnoConDatos r = e.turnoService.reprogramar(new ReprogramarTurnoCommand(id, null, LocalTime.of(11, 0), null));
        assertEquals(en(MANANA, "11:00"), r.turno().getRango().inicio());
        assertEquals(en(MANANA, "11:45"), r.turno().getRango().fin());
    }

    @Test
    @DisplayName("Reprogramar dentro del mismo rango no choca consigo mismo")
    void noChocaConsigoMismo() {
        long id = e.agendar(ana, lopez, MANANA, "10:00", 60).turno().getId();
        TurnoConDatos r = e.turnoService.reprogramar(new ReprogramarTurnoCommand(id, null, LocalTime.of(10, 30), null));
        assertEquals(en(MANANA, "10:30"), r.turno().getRango().inicio());
    }

    @Test
    @DisplayName("Si el nuevo horario se cruza, se rechaza y el turno conserva el original")
    void reprogramarConCruce() {
        e.agendar(luis, lopez, MANANA, "11:00", 30);
        long id = e.agendar(ana, lopez, MANANA, "09:00", 30).turno().getId();
        assertThrows(TurnoSolapadoException.class,
                () -> e.turnoService.reprogramar(new ReprogramarTurnoCommand(id, null, LocalTime.of(11, 15), null)));
        assertEquals(en(MANANA, "09:00"), e.turnoService.porId(id).turno().getRango().inicio());
    }

    @Test
    void reprogramarValidaPasadoYDisponibilidad() {
        long id = e.agendar(ana, lopez, MANANA, "09:00", 30).turno().getId();
        assertThrows(ValorInvalidoException.class,
                () -> e.turnoService.reprogramar(new ReprogramarTurnoCommand(id, HOY, LocalTime.of(8, 0), null)));
        assertThrows(ProfesionalNoDisponibleException.class,
                () -> e.turnoService.reprogramar(new ReprogramarTurnoCommand(id, SABADO, null, null)));
        assertThrows(ValorInvalidoException.class,
                () -> e.turnoService.reprogramar(new ReprogramarTurnoCommand(id, null, null, null)));
        assertEquals(en(MANANA, "09:00"), e.turnoService.porId(id).turno().getRango().inicio());
    }

    // ---------------- HU-12: consultar ----------------

    private void agendaDeEjemplo() {
        e.agendar(ana, lopez, MANANA.plusDays(1), "09:00", 30);   // jueves
        e.agendar(luis, lopez, MANANA, "10:00", 30);              // miércoles
        e.agendar(ana, gomez, MANANA, "08:00", 30);               // miércoles, otro profesional
        long cancelado = e.agendar(luis, gomez, MANANA, "09:00", 30).turno().getId();
        e.turnoService.cancelar(cancelado);
    }

    private List<TurnoConDatos> consulta(Long prof, Long cli, java.time.LocalDate d, java.time.LocalDate h, boolean cancelados) {
        return e.turnoService.consultar(new ConsultarTurnosQuery(prof, cli, d, h, cancelados));
    }

    @Test
    @DisplayName("Sin filtros trae todo lo vigente, ordenado por fecha y hora de inicio")
    void sinFiltros() {
        agendaDeEjemplo();
        List<TurnoConDatos> r = consulta(null, null, null, null, false);
        assertEquals(3, r.size());
        assertEquals(en(MANANA, "08:00"), r.get(0).turno().getRango().inicio());
        assertEquals(en(MANANA, "10:00"), r.get(1).turno().getRango().inicio());
        assertEquals(en(MANANA.plusDays(1), "09:00"), r.get(2).turno().getRango().inicio());
        assertEquals("Dr. Gómez", r.get(0).profesional().getNombre());
    }

    @Test
    void filtraPorProfesionalPorClienteYPorFechas() {
        agendaDeEjemplo();
        assertEquals(2, consulta(lopez, null, null, null, false).size());
        assertEquals(2, consulta(null, ana, null, null, false).size());
        assertEquals(2, consulta(null, null, MANANA, MANANA, false).size());
        assertEquals(1, consulta(null, null, MANANA.plusDays(1), null, false).size());
        assertEquals(2, consulta(null, null, null, MANANA, false).size());
    }

    @Test
    @DisplayName("Los filtros se combinan")
    void combinaFiltros() {
        agendaDeEjemplo();
        assertEquals(1, consulta(lopez, ana, null, null, false).size());
        assertEquals(1, consulta(lopez, null, MANANA, MANANA, false).size());
        assertEquals(0, consulta(gomez, luis, null, null, false).size());   // su único turno está cancelado
    }

    @Test
    void canceladosSoloSiSePiden() {
        agendaDeEjemplo();
        List<TurnoConDatos> r = consulta(gomez, luis, null, null, true);
        assertEquals(1, r.size());
        assertEquals(EstadoTurno.CANCELADO, r.get(0).turno().getEstado());
        assertEquals(4, consulta(null, null, null, null, true).size());
    }

    @Test
    void sinResultadosEsListaVacia() {
        assertEquals(0, consulta(null, null, null, null, false).size());
        agendaDeEjemplo();
        assertEquals(0, consulta(null, null, SABADO, SABADO, false).size());
    }

    @Test
    void validaLosFiltros() {
        assertThrows(ValorInvalidoException.class, () -> consulta(null, null, MANANA.plusDays(1), MANANA, false));
        assertThrows(ProfesionalNoEncontradoException.class, () -> consulta(99L, null, null, null, false));
        assertThrows(ClienteNoEncontradoException.class, () -> consulta(null, 99L, null, null, false));
    }

    @Test
    void consultaPorId() {
        long id = e.agendar(ana, lopez, MANANA, "10:00", 30).turno().getId();
        assertEquals("Ana Pérez", e.turnoService.porId(id).cliente().getNombre());
        assertThrows(TurnoNoEncontradoException.class, () -> e.turnoService.porId(99));
    }
}
