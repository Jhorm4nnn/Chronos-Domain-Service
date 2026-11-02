package com.sistematurnos.agenda.domain.vo;

import com.sistematurnos.agenda.domain.exception.ValorInvalidoException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Intervalo semiabierto [inicio, fin) dentro de un mismo día. Que un turno termine justo cuando empieza otro NO es cruce.
 */
public record RangoTiempo(LocalDateTime inicio, LocalDateTime fin) {

    public static final int DURACION_POR_DEFECTO = 30;
    public static final int DURACION_MINIMA = 5;
    public static final int DURACION_MAXIMA = 480;

    public RangoTiempo {
        if (inicio == null || fin == null) {
            throw new ValorInvalidoException("El turno necesita fecha y hora de inicio y de fin");
        }
        if (!fin.isAfter(inicio)) {
            throw new ValorInvalidoException("La hora de fin debe ser posterior a la de inicio");
        }
        if (!inicio.toLocalDate().equals(fin.toLocalDate())) {
            throw new ValorInvalidoException("El turno debe terminar el mismo día en que empieza");
        }
    }

    /** Factory: inicio + duración en minutos (null = 30). */
    public static RangoTiempo de(LocalDateTime inicio, Integer minutos) {
        int duracion = minutos == null ? DURACION_POR_DEFECTO : minutos;
        if (duracion < DURACION_MINIMA || duracion > DURACION_MAXIMA) {
            throw new ValorInvalidoException("La duración debe estar entre " + DURACION_MINIMA + " y " + DURACION_MAXIMA + " minutos");
        }
        if (inicio == null) {
            throw new ValorInvalidoException("El turno necesita fecha y hora de inicio");
        }
        return new RangoTiempo(inicio, inicio.plusMinutes(duracion));
    }

    public boolean seCruzaCon(RangoTiempo otro) {
        return inicio.isBefore(otro.fin) && otro.inicio.isBefore(fin);
    }

    public long duracionMinutos() {
        return Duration.between(inicio, fin).toMinutes();
    }

    public String descripcion() {
        return inicio.toLocalDate() + " " + inicio.format(DateTimeFormatter.ofPattern("HH:mm"))
                + " a " + fin.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}
