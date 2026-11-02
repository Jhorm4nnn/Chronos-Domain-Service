package com.sistematurnos.agenda.contract;

import static org.junit.jupiter.api.Assertions.*;

import com.sistematurnos.agenda.application.port.out.ProfesionalRepositoryPort;
import com.sistematurnos.agenda.domain.model.Profesional;
import com.sistematurnos.agenda.testsupport.Datos;
import java.util.List;
import org.junit.jupiter.api.Test;

public abstract class ProfesionalRepositoryContractTest {

    protected abstract ProfesionalRepositoryPort repo();

    @Test
    void guardaConSuDisponibilidad() {
        Profesional g = repo().guardar(Datos.doctora());
        assertNotNull(g.getId());
        Profesional r = repo().buscarPorId(g.getId().longValue()).orElseThrow();
        assertEquals("Dra. López", r.getNombre());
        assertEquals(10, r.getDisponibilidad().size());
        assertTrue(r.atiende(Datos.rango(Datos.MANANA, "09:00", 30)));
        assertFalse(r.atiende(Datos.rango(Datos.SABADO, "09:00", 30)));
    }

    @Test
    void buscaPorNombreSinImportarMayusculas() {
        repo().guardar(Datos.doctora());
        assertTrue(repo().buscarPorNombre("dra. LÓPEZ").isPresent());
        assertTrue(repo().buscarPorNombre("Otro").isEmpty());
    }

    @Test
    void buscarParaAgendarDevuelveElProfesional() {
        Profesional g = repo().guardar(Datos.doctora());
        assertTrue(repo().buscarParaAgendar(g.getId().longValue()).isPresent());
        assertTrue(repo().buscarParaAgendar(9999).isEmpty());
    }

    @Test
    void listaYBuscaVariosPorId() {
        Profesional a = repo().guardar(Datos.doctora());
        Profesional b = repo().guardar(Profesional.registrar("Dr. Gómez", Datos.semanaLaboral()));
        assertEquals(2, repo().listar().size());
        assertEquals(a.getId(), repo().listar().get(0).getId());
        assertEquals(2, repo().buscarPorIds(List.of(a.getId(), b.getId())).size());
        assertEquals(1, repo().buscarPorIds(List.of(b.getId(), 9999L)).size());
    }
}
