package com.sistematurnos.agenda.infrastructure.adapters.in.cli;

import com.sistematurnos.agenda.application.port.in.ClienteUseCase;
import com.sistematurnos.agenda.application.port.in.ProfesionalUseCase;
import com.sistematurnos.agenda.application.port.in.TurnoUseCase;
import com.sistematurnos.agenda.application.port.in.command.*;
import com.sistematurnos.agenda.application.port.in.result.TurnoConDatos;
import com.sistematurnos.agenda.domain.exception.DominioException;
import com.sistematurnos.agenda.domain.model.Cliente;
import com.sistematurnos.agenda.domain.model.Profesional;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Segundo adaptador de ENTRADA (RNF-01): la misma lógica de negocio, operada por línea de comandos.
 * Solo conoce los casos de uso (puertos de entrada); no contiene ninguna regla de negocio. Es Java puro: se prueba sin Spring.
 */
public class TurnosCli {

    private static final Map<String, DayOfWeek> DIAS = Map.ofEntries(
            Map.entry("LUNES", DayOfWeek.MONDAY), Map.entry("MARTES", DayOfWeek.TUESDAY),
            Map.entry("MIERCOLES", DayOfWeek.WEDNESDAY), Map.entry("MIÉRCOLES", DayOfWeek.WEDNESDAY),
            Map.entry("JUEVES", DayOfWeek.THURSDAY), Map.entry("VIERNES", DayOfWeek.FRIDAY),
            Map.entry("SABADO", DayOfWeek.SATURDAY), Map.entry("SÁBADO", DayOfWeek.SATURDAY),
            Map.entry("DOMINGO", DayOfWeek.SUNDAY));

    private static final String AYUDA = String.join("\n",
            "Comandos:",
            "  cliente registrar <nombre> | <telefono> | [correo]",
            "  cliente listar",
            "  profesional registrar <nombre> | <DIA HH:mm-HH:mm>, <DIA HH:mm-HH:mm>, ...   (ej: LUNES 08:00-12:00)",
            "  profesional listar",
            "  turno agendar <clienteId> <profesionalId> <AAAA-MM-DD> <HH:mm> [minutos]",
            "  turno cancelar <turnoId>",
            "  turno reprogramar <turnoId> <AAAA-MM-DD> <HH:mm> [minutos]",
            "  turno ver <turnoId>",
            "  turno listar [profesional=ID] [cliente=ID] [desde=AAAA-MM-DD] [hasta=AAAA-MM-DD] [cancelados=si]",
            "  ayuda | salir");

    private final ClienteUseCase clientes;
    private final ProfesionalUseCase profesionales;
    private final TurnoUseCase turnos;
    private final InputStream entrada;
    private final PrintStream salida;

    public TurnosCli(ClienteUseCase clientes, ProfesionalUseCase profesionales, TurnoUseCase turnos,
                     InputStream entrada, PrintStream salida) {
        this.clientes = clientes;
        this.profesionales = profesionales;
        this.turnos = turnos;
        this.entrada = entrada;
        this.salida = salida;
    }

    /** Lee comandos hasta «salir» o fin de la entrada. */
    public void ejecutar() throws IOException {
        salida.println("Sistema de Turnos (CLI). Escriba «ayuda» para ver los comandos.");
        BufferedReader lector = new BufferedReader(new InputStreamReader(entrada, StandardCharsets.UTF_8));
        String linea;
        salida.print("> ");
        salida.flush();
        while ((linea = lector.readLine()) != null) {
            if (!procesar(linea)) {
                return;
            }
            salida.print("> ");
            salida.flush();
        }
    }

    /** Ejecuta una línea. Devuelve false cuando hay que terminar. Nunca deja escapar una excepción ni imprime trazas. */
    public boolean procesar(String linea) {
        String texto = linea == null ? "" : linea.trim();
        if (texto.isEmpty()) {
            return true;
        }
        try {
            String[] partes = texto.split("\\s+", 3);
            String grupo = partes[0].toLowerCase(Locale.ROOT);
            String accion = partes.length > 1 ? partes[1].toLowerCase(Locale.ROOT) : "";
            String resto = partes.length > 2 ? partes[2] : "";
            switch (grupo) {
                case "salir" -> { return false; }
                case "ayuda" -> salida.println(AYUDA);
                case "cliente" -> cliente(accion, resto);
                case "profesional" -> profesional(accion, resto);
                case "turno" -> turno(accion, resto);
                default -> salida.println("Error: comando desconocido. Escriba «ayuda».");
            }
        } catch (DominioException e) {
            salida.println("Error: " + e.getMessage());
        } catch (RuntimeException e) {
            salida.println("Error: el comando no tiene el formato esperado. Escriba «ayuda» para ver ejemplos.");
        }
        return true;
    }

    private void cliente(String accion, String resto) {
        switch (accion) {
            case "registrar" -> {
                String[] c = campos(resto);
                Cliente creado = clientes.registrar(new RegistrarClienteCommand(c[0], c[1], c[2]));
                salida.println("Cliente registrado: " + describir(creado));
            }
            case "listar" -> clientes.listar().forEach(c -> salida.println(describir(c)));
            default -> salida.println("Error: use «cliente registrar» o «cliente listar».");
        }
    }

    private void profesional(String accion, String resto) {
        switch (accion) {
            case "registrar" -> {
                String[] c = campos(resto);
                Profesional creado = profesionales.registrar(new RegistrarProfesionalCommand(c[0], franjas(c[1])));
                salida.println("Profesional registrado: " + describir(creado));
            }
            case "listar" -> profesionales.listar().forEach(p -> salida.println(describir(p)));
            default -> salida.println("Error: use «profesional registrar» o «profesional listar».");
        }
    }

    private void turno(String accion, String resto) {
        String[] a = resto.isBlank() ? new String[0] : resto.trim().split("\\s+");
        switch (accion) {
            case "agendar" -> {
                TurnoConDatos t = turnos.agendar(new AgendarTurnoCommand(Long.parseLong(a[0]), Long.parseLong(a[1]),
                        LocalDate.parse(a[2]), LocalTime.parse(a[3]), a.length > 4 ? Integer.valueOf(a[4]) : null));
                salida.println("Turno agendado: " + describir(t));
            }
            case "cancelar" -> salida.println("Turno cancelado: " + describir(turnos.cancelar(Long.parseLong(a[0]))));
            case "reprogramar" -> {
                TurnoConDatos t = turnos.reprogramar(new ReprogramarTurnoCommand(Long.parseLong(a[0]),
                        LocalDate.parse(a[1]), LocalTime.parse(a[2]), a.length > 3 ? Integer.valueOf(a[3]) : null));
                salida.println("Turno reprogramado: " + describir(t));
            }
            case "ver" -> salida.println(describir(turnos.porId(Long.parseLong(a[0]))));
            case "listar" -> {
                Long profesionalId = null, clienteId = null;
                LocalDate desde = null, hasta = null;
                boolean cancelados = false;
                for (String filtro : a) {
                    String[] kv = filtro.split("=", 2);
                    if (kv.length != 2) {
                        throw new IllegalArgumentException(filtro);
                    }
                    switch (kv[0].toLowerCase(Locale.ROOT)) {
                        case "profesional" -> profesionalId = Long.parseLong(kv[1]);
                        case "cliente" -> clienteId = Long.parseLong(kv[1]);
                        case "desde" -> desde = LocalDate.parse(kv[1]);
                        case "hasta" -> hasta = LocalDate.parse(kv[1]);
                        case "cancelados" -> cancelados = kv[1].equalsIgnoreCase("si");
                        default -> throw new IllegalArgumentException(filtro);
                    }
                }
                List<TurnoConDatos> r = turnos.consultar(new ConsultarTurnosQuery(profesionalId, clienteId, desde, hasta, cancelados));
                if (r.isEmpty()) {
                    salida.println("(sin turnos)");
                }
                r.forEach(t -> salida.println(describir(t)));
            }
            default -> salida.println("Error: use «turno agendar|cancelar|reprogramar|ver|listar».");
        }
    }

    /** Separa por «|» y devuelve siempre tres campos (los que no vengan, nulos). */
    private static String[] campos(String resto) {
        String[] partes = resto.split("\\|", -1);
        String[] r = new String[3];
        for (int i = 0; i < 3; i++) {
            r[i] = i < partes.length && !partes[i].isBlank() ? partes[i].trim() : null;
        }
        return r;
    }

    private static List<RegistrarProfesionalCommand.FranjaCommand> franjas(String texto) {
        List<RegistrarProfesionalCommand.FranjaCommand> r = new ArrayList<>();
        if (texto == null) {
            return r;
        }
        for (String f : texto.split(",")) {
            String[] dh = f.trim().split("\\s+");
            String[] horas = dh[1].split("-");
            DayOfWeek dia = DIAS.get(dh[0].toUpperCase(Locale.ROOT));
            if (dia == null) {
                dia = DayOfWeek.valueOf(dh[0].toUpperCase(Locale.ROOT));
            }
            r.add(new RegistrarProfesionalCommand.FranjaCommand(dia, LocalTime.parse(horas[0]), LocalTime.parse(horas[1])));
        }
        return r;
    }

    private static String describir(Cliente c) {
        return "#" + c.getId() + " " + c.getNombre() + " · tel " + c.getTelefono().valor() + (c.getCorreo() == null ? "" : " · " + c.getCorreo());
    }

    private static String describir(Profesional p) {
        return "#" + p.getId() + " " + p.getNombre() + " · " + p.getDisponibilidad().size() + " franjas";
    }

    private static String describir(TurnoConDatos t) {
        return "Turno #" + t.turno().getId() + " · " + t.turno().getRango().descripcion() + " · " + t.profesional().getNombre()
                + " · " + t.cliente().getNombre() + " · " + t.turno().getEstado();
    }
}
