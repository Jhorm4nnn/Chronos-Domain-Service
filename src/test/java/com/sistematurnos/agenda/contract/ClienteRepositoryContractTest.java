package com.sistematurnos.agenda.contract;

import static org.junit.jupiter.api.Assertions.*;

import com.sistematurnos.agenda.application.port.out.ClienteRepositoryPort;
import com.sistematurnos.agenda.domain.model.Cliente;
import com.sistematurnos.agenda.domain.vo.Telefono;
import com.sistematurnos.agenda.testsupport.Datos;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Toda implementación del puerto (memoria, JPA…) debe pasar estas mismas pruebas. */
public abstract class ClienteRepositoryContractTest {

    protected abstract ClienteRepositoryPort repo();

    @Test
    void guardaYRecupera() {
        Cliente g = repo().guardar(Datos.ana());
        assertNotNull(g.getId());
        Cliente r = repo().buscarPorId(g.getId().longValue()).orElseThrow();
        assertEquals("Ana Pérez", r.getNombre());
        assertEquals("3001234567", r.getTelefono().valor());
        assertEquals("ana@correo.com", r.getCorreo());
    }

    @Test
    void buscaPorTelefono() {
        repo().guardar(Datos.ana());
        assertTrue(repo().buscarPorTelefono(Telefono.de("3001234567")).isPresent());
        assertTrue(repo().buscarPorTelefono(Telefono.de("3000000000")).isEmpty());
    }

    @Test
    void listaYBuscaVariosPorId() {
        Cliente a = repo().guardar(Datos.ana());
        Cliente b = repo().guardar(Cliente.registrar("Luis", Telefono.de("3109999999"), null));
        assertEquals(2, repo().listar().size());
        assertEquals(a.getId(), repo().listar().get(0).getId());
        assertEquals(2, repo().buscarPorIds(List.of(a.getId(), b.getId())).size());
        assertEquals(1, repo().buscarPorIds(List.of(a.getId(), 9999L)).size());
        assertNull(repo().buscarPorId(b.getId().longValue()).orElseThrow().getCorreo());
    }

    @Test
    void inexistenteDevuelveVacio() {
        assertTrue(repo().buscarPorId(12345).isEmpty());
    }
}
