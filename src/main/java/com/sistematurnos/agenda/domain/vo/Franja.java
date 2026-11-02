package com.sistematurnos.agenda.domain.vo;

import com.sistematurnos.agenda.domain.exception.ValorInvalidoException;
import java.time.DayOfWeek;
import java.time.LocalTime;

/** Un tramo de disponibilidad semanal: por ejemplo, lunes de 08:00 a 12:00. */
public record Franja(DayOfWeek dia, LocalTime desde, LocalTime hasta) {

    public Franja {
        if (dia == null || desde == null || hasta == null) {
            throw new ValorInvalidoException("Cada franja necesita día, hora de inicio y hora de fin");
        }
        if (!hasta.isAfter(desde)) {
            throw new ValorInvalidoException("En la franja del " + dia + " la hora de fin debe ser posterior a la de inicio");
        }
    }

    public boolean contiene(RangoTiempo rango) {
        return rango.inicio().getDayOfWeek() == dia
                && !rango.inicio().toLocalTime().isBefore(desde)
                && !rango.fin().toLocalTime().isAfter(hasta);
    }

    public boolean seTraslapaCon(Franja otra) {
        return dia == otra.dia && desde.isBefore(otra.hasta) && otra.desde.isBefore(hasta);
    }
}
