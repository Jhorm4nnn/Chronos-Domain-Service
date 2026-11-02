package com.sistematurnos.agenda.contract;

import static org.junit.jupiter.api.Assertions.*;

import com.sistematurnos.agenda.application.port.out.FiltroTurno;
import com.sistematurnos.agenda.application.port.out.TurnoRepositoryPort;
import com.sistematurnos.agenda.domain.model.EstadoTurno;
import com.sistematurnos.agenda.domain.model.Turno;
import com.sistematurnos.agenda.testsupport.Datos;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

public abstract class TurnoRepositoryContractTest {

    protected abstract TurnoRepositoryPort repo();

    /** Crea un cliente real (si la implementación lo necesita) y devuelve su id. */
    protected abstract long crearCliente(String telefono);

    /** Crea un profesional real (si la implementación lo necesita) y devuelve su id. */
    protected abstract long crearProfesional(String nombre);

    private Turno turno(long cliente, long prof, LocalDate fecha, String hora) {
        return Turno.agendar(cliente, prof, Datos.rango(fecha, hora, 30));
    }

    @Test
    void guardaYRecupera() {
        long c = crearCliente("3001111111"), p = crearProfesional("Dra. A");
        Turno g = repo().guardar(turno(c, p, Datos.MANANA, "09:00"));
        assertNotNull(g.getId());
        Turno r = repo().buscarPorId(g.getId().longValue()).orElseThrow();
        assertEquals(c, r.getClienteId());
        assertEquals(p, r.getProfesionalId());
        assertEquals(Datos.en(Datos.MANANA, "09:30"), r.getRango().fin());
        assertEquals(EstadoTurno.AGENDADO, r.getEstado());
    }

    @Test
    void persisteCancelacionYReprogramacion() {
        long c = crearCliente("3001111111"), p = crearProfesional("Dra. A");
        Turno g = repo().guardar(turno(c, p, Datos.MANANA, "09:00"));
        g.reprogramar(Datos.rango(Datos.MANANA, "11:00", 45), Datos.AHORA);
        repo().guardar(g);
        assertEquals(Datos.en(Datos.MANANA, "11:45"), repo().buscarPorId(g.getId().longValue()).orElseThrow().getRango().fin());
        g.cancelar(Datos.AHORA);
        repo().guardar(g);
        assertEquals(EstadoTurno.CANCELADO, repo().buscarPorId(g.getId().longValue()).orElseThrow().getEstado());
    }

    @Test
    void agendadosDelProfesionalEnUnaFecha() {
        long c = crearCliente("3001111111"), p = crearProfesional("Dra. A"), q = crearProfesional("Dr. B");
        repo().guardar(turno(c, p, Datos.MANANA, "09:00"));
        repo().guardar(turno(c, p, Datos.MANANA, "10:00"));
        repo().guardar(turno(c, p, Datos.MANANA.plusDays(1), "09:00"));
        repo().guardar(turno(c, q, Datos.MANANA, "09:00"));
        Turno cancelado = repo().guardar(turno(c, p, Datos.MANANA, "11:00"));
        cancelado.cancelar(Datos.AHORA);
        repo().guardar(cancelado);
        assertEquals(2, repo().buscarAgendadosDelProfesional(p, Datos.MANANA).size());
        assertEquals(1, repo().buscarAgendadosDelProfesional(q, Datos.MANANA).size());
        assertEquals(0, repo().buscarAgendadosDelProfesional(p, Datos.SABADO).size());
    }

    @Test
    void buscaConFiltrosOrdenadoPorInicio() {
        long c1 = crearCliente("3001111111"), c2 = crearCliente("3002222222");
        long p1 = crearProfesional("Dra. A"), p2 = crearProfesional("Dr. B");
        repo().guardar(turno(c1, p1, Datos.MANANA.plusDays(1), "09:00"));
        repo().guardar(turno(c2, p1, Datos.MANANA, "10:00"));
        repo().guardar(turno(c1, p2, Datos.MANANA, "08:00"));
        Turno cancelado = repo().guardar(turno(c2, p2, Datos.MANANA, "09:00"));
        cancelado.cancelar(Datos.AHORA);
        repo().guardar(cancelado);

        List<Turno> todos = repo().buscar(new FiltroTurno(null, null, null, null, false));
        assertEquals(3, todos.size());
        assertEquals(Datos.en(Datos.MANANA, "08:00"), todos.get(0).getRango().inicio());
        assertEquals(Datos.en(Datos.MANANA.plusDays(1), "09:00"), todos.get(2).getRango().inicio());
        assertEquals(4, repo().buscar(new FiltroTurno(null, null, null, null, true)).size());
        assertEquals(2, repo().buscar(new FiltroTurno(p1, null, null, null, false)).size());
        assertEquals(2, repo().buscar(new FiltroTurno(null, c1, null, null, false)).size());
        assertEquals(2, repo().buscar(new FiltroTurno(null, null, Datos.MANANA, Datos.MANANA, false)).size());
        assertEquals(1, repo().buscar(new FiltroTurno(p1, c1, null, null, false)).size());
        assertEquals(1, repo().buscar(new FiltroTurno(null, null, Datos.MANANA.plusDays(1), null, false)).size());
    }

    @Test
    void inexistenteDevuelveVacio() {
        assertTrue(repo().buscarPorId(4321).isEmpty());
    }
}
