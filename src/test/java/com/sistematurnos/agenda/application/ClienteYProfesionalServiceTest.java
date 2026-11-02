package com.sistematurnos.agenda.application;

import static org.junit.jupiter.api.Assertions.*;

import com.sistematurnos.agenda.application.port.in.command.RegistrarClienteCommand;
import com.sistematurnos.agenda.application.port.in.command.RegistrarProfesionalCommand;
import com.sistematurnos.agenda.application.port.in.command.RegistrarProfesionalCommand.FranjaCommand;
import com.sistematurnos.agenda.domain.exception.*;
import com.sistematurnos.agenda.domain.model.Cliente;
import com.sistematurnos.agenda.domain.model.Profesional;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClienteYProfesionalServiceTest {

    private final Escenario e = new Escenario();

    @Test
    void registraUnCliente() {
        Cliente c = e.clienteService.registrar(new RegistrarClienteCommand("Ana Pérez", "300 123 4567", "ana@correo.com"));
        assertNotNull(c.getId());
        assertEquals("3001234567", c.getTelefono().valor());
        assertEquals("Ana Pérez", e.clienteService.porId(c.getId()).getNombre());
        assertEquals(1, e.clienteService.listar().size());
    }

    @Test
    @DisplayName("Si faltan datos, el mensaje lista TODOS los campos faltantes y no se guarda nada")
    void clienteListaLosFaltantes() {
        ValorInvalidoException ex = assertThrows(ValorInvalidoException.class,
                () -> e.clienteService.registrar(new RegistrarClienteCommand(" ", null, null)));
        assertTrue(ex.getMessage().contains("nombre"));
        assertTrue(ex.getMessage().contains("teléfono"));
        assertEquals(0, e.clienteService.listar().size());
    }

    @Test
    void noRepiteTelefonoAunqueVengaConOtroFormato() {
        e.cliente("Ana", "300 123 4567");
        assertThrows(ClienteDuplicadoException.class, () -> e.cliente("Otra Ana", "300-123-4567"));
        e.cliente("Luis", "3109999999");
        assertEquals(2, e.clienteService.listar().size());
    }

    @Test
    void clienteInexistente() {
        assertThrows(ClienteNoEncontradoException.class, () -> e.clienteService.porId(99));
    }

    @Test
    void registraUnProfesionalConSuHorario() {
        Profesional p = e.doctora("Dra. López");
        assertNotNull(p.getId());
        assertEquals(10, p.getDisponibilidad().size());
        assertEquals("Dra. López", e.profesionalService.porId(p.getId()).getNombre());
    }

    @Test
    void profesionalListaLosFaltantesYValidaFranjas() {
        ValorInvalidoException ex = assertThrows(ValorInvalidoException.class,
                () -> e.profesionalService.registrar(new RegistrarProfesionalCommand(null, null)));
        assertTrue(ex.getMessage().contains("nombre"));
        assertTrue(ex.getMessage().contains("disponibilidad"));
        assertThrows(ValorInvalidoException.class, () -> e.profesionalService.registrar(new RegistrarProfesionalCommand("Dr. X",
                List.of(new FranjaCommand(DayOfWeek.MONDAY, LocalTime.of(12, 0), LocalTime.of(8, 0))))));
        assertEquals(0, e.profesionalService.listar().size());
    }

    @Test
    void noRepiteNombreDeProfesionalSinImportarMayusculas() {
        e.doctora("Dra. López");
        assertThrows(ProfesionalDuplicadoException.class, () -> e.doctora("dra. LÓPEZ"));
        e.doctora("Dr. Gómez");
        assertEquals(2, e.profesionalService.listar().size());
        assertThrows(ProfesionalNoEncontradoException.class, () -> e.profesionalService.porId(99));
    }
}
