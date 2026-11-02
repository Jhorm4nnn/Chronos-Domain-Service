package com.sistematurnos.agenda.infrastructure.adapters.in.cli;

import static org.junit.jupiter.api.Assertions.*;

import com.sistematurnos.agenda.application.service.*;
import com.sistematurnos.agenda.domain.service.ValidadorAgenda;
import com.sistematurnos.agenda.testsupport.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** El CLI usa los MISMOS casos de uso que el REST: aquí se comprueba con los mismos servicios y dobles en memoria. */
class TurnosCliTest {

    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    private final PrintStream salida = new PrintStream(bytes, true, StandardCharsets.UTF_8);
    private final TurnosCli cli;

    TurnosCliTest() {
        ClienteRepositoryEnMemoria clientes = new ClienteRepositoryEnMemoria();
        ProfesionalRepositoryEnMemoria profesionales = new ProfesionalRepositoryEnMemoria();
        TurnoRepositoryEnMemoria turnos = new TurnoRepositoryEnMemoria();
        cli = new TurnosCli(new ClienteService(clientes), new ProfesionalService(profesionales),
                new TurnoService(clientes, profesionales, turnos, new TransaccionDirecta(), Datos.relojFijo(), new ValidadorAgenda()),
                new ByteArrayInputStream(new byte[0]), salida);
    }

    private String correr(String... lineas) {
        bytes.reset();
        for (String l : lineas) {
            cli.procesar(l);
        }
        return bytes.toString(StandardCharsets.UTF_8);
    }

    private void preparar() {
        correr("cliente registrar Ana Pérez | 300 123 4567 | ana@correo.com",
               "profesional registrar Dra. López | LUNES 08:00-12:00, MIERCOLES 08:00-12:00, MIERCOLES 14:00-18:00");
    }

    @Test
    @DisplayName("Flujo completo por CLI: registrar, agendar, cruce, reprogramar, listar y cancelar")
    void flujo() {
        preparar();
        assertTrue(correr("cliente listar").contains("Ana Pérez"));
        assertTrue(correr("profesional listar").contains("3 franjas"));
        assertTrue(correr("turno agendar 1 1 2026-10-21 09:00 30").contains("Turno agendado: Turno #1"));
        String cruce = correr("turno agendar 1 1 2026-10-21 09:15");
        assertTrue(cruce.startsWith("Error: El horario se cruza con el turno 1"));
        assertTrue(correr("turno reprogramar 1 2026-10-21 10:00").contains("10:00 a 10:30"));
        assertTrue(correr("turno listar profesional=1 desde=2026-10-21").contains("Turno #1"));
        assertTrue(correr("turno cancelar 1").contains("CANCELADO"));
        assertTrue(correr("turno listar").contains("(sin turnos)"));
        assertTrue(correr("turno listar cancelados=si").contains("CANCELADO"));
    }

    @Test
    @DisplayName("Los errores de negocio salen con un mensaje claro y sin trazas técnicas")
    void erroresClaros() {
        preparar();
        assertTrue(correr("turno agendar 1 1 2026-10-24 09:00").contains("no atiende en ese horario"));
        assertTrue(correr("turno agendar 9 1 2026-10-21 09:00").contains("No existe el cliente con id 9"));
        assertTrue(correr("cliente registrar | |").contains("Faltan campos obligatorios: nombre, teléfono"));
        assertTrue(correr("cliente registrar Otro | 300-123-4567").contains("Ya existe un cliente"));
        assertTrue(correr("turno agendar 1 1 2026-10-20 08:00").contains("anteriores a la actual"));
    }

    @Test
    void formatoInvalidoNoExponeDetallesTecnicos() {
        String r = correr("turno agendar uno dos manana temprano");
        assertTrue(r.contains("no tiene el formato esperado"));
        assertFalse(r.contains("Exception"));
        assertFalse(r.contains("NumberFormat"));
        assertTrue(correr("turno agendar 1").contains("no tiene el formato esperado"));
        assertTrue(correr("turno listar foo=1").contains("no tiene el formato esperado"));
        assertTrue(correr("baila").contains("comando desconocido"));
        assertTrue(correr("ayuda").contains("turno agendar"));
    }

    @Test
    void salirTerminaYLasLineasVaciasSeIgnoran() {
        assertTrue(cli.procesar(""));
        assertFalse(cli.procesar("salir"));
    }
}
