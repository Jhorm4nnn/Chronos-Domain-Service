package com.sistematurnos.agenda.testsupport;

import com.sistematurnos.agenda.domain.model.Cliente;
import com.sistematurnos.agenda.domain.model.Profesional;
import com.sistematurnos.agenda.domain.vo.Franja;
import com.sistematurnos.agenda.domain.vo.RangoTiempo;
import com.sistematurnos.agenda.domain.vo.Telefono;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/** Datos de ejemplo. El reloj fijo es martes 20 de octubre de 2026, 10:00 en Bogotá. */
public final class Datos {

    public static final LocalDate HOY = LocalDate.of(2026, 10, 20);          // martes
    public static final LocalDate MANANA = HOY.plusDays(1);                  // miércoles
    public static final LocalDate SABADO = LocalDate.of(2026, 10, 24);
    public static final LocalDateTime AHORA = LocalDateTime.of(HOY, LocalTime.of(10, 0));

    private Datos() { }

    public static Clock relojFijo() {
        return Clock.fixed(Instant.parse("2026-10-20T15:00:00Z"), ZoneId.of("America/Bogota"));
    }

    public static LocalDateTime en(LocalDate fecha, String hora) {
        return LocalDateTime.of(fecha, LocalTime.parse(hora));
    }

    /** Lunes a viernes, 08:00-12:00 y 14:00-18:00. */
    public static List<Franja> semanaLaboral() {
        List<Franja> franjas = new ArrayList<>();
        for (DayOfWeek d : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
            franjas.add(new Franja(d, LocalTime.of(8, 0), LocalTime.of(12, 0)));
            franjas.add(new Franja(d, LocalTime.of(14, 0), LocalTime.of(18, 0)));
        }
        return franjas;
    }

    public static Profesional doctora() {
        return Profesional.registrar("Dra. López", semanaLaboral());
    }

    public static Cliente ana() {
        return Cliente.registrar("Ana Pérez", Telefono.de("300 123 4567"), "ana@correo.com");
    }

    public static RangoTiempo rango(LocalDate fecha, String hora, int minutos) {
        return RangoTiempo.de(en(fecha, hora), minutos);
    }
}
