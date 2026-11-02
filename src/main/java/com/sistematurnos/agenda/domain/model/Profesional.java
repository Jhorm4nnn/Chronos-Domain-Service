package com.sistematurnos.agenda.domain.model;

import com.sistematurnos.agenda.domain.exception.ProfesionalNoDisponibleException;
import com.sistematurnos.agenda.domain.exception.ValorInvalidoException;
import com.sistematurnos.agenda.domain.vo.Franja;
import com.sistematurnos.agenda.domain.vo.RangoTiempo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Profesional {

    private final Long id;
    private final String nombre;
    private final List<Franja> disponibilidad;

    private Profesional(Long id, String nombre, List<Franja> disponibilidad) {
        if (nombre == null || nombre.isBlank()) {
            throw new ValorInvalidoException("El nombre del profesional es obligatorio");
        }
        if (nombre.trim().length() > 120) {
            throw new ValorInvalidoException("El nombre no puede superar 120 caracteres");
        }
        if (disponibilidad == null || disponibilidad.isEmpty()) {
            throw new ValorInvalidoException("El profesional necesita al menos una franja de disponibilidad");
        }
        List<Franja> copia = new ArrayList<>(disponibilidad);
        for (int i = 0; i < copia.size(); i++) {
            for (int j = i + 1; j < copia.size(); j++) {
                if (copia.get(i).seTraslapaCon(copia.get(j))) {
                    throw new ValorInvalidoException("Dos franjas del " + copia.get(i).dia() + " se traslapan");
                }
            }
        }
        this.id = id;
        this.nombre = nombre.trim();
        this.disponibilidad = copia;
    }

    public static Profesional registrar(String nombre, List<Franja> disponibilidad) {
        return new Profesional(null, nombre, disponibilidad);
    }

    public static Profesional reconstruir(long id, String nombre, List<Franja> disponibilidad) {
        return new Profesional(id, nombre, disponibilidad);
    }

    public boolean atiende(RangoTiempo rango) {
        return disponibilidad.stream().anyMatch(f -> f.contiene(rango));
    }

    public void asegurarQueAtiende(RangoTiempo rango) {
        if (!atiende(rango)) {
            throw new ProfesionalNoDisponibleException(nombre, "Pidió " + rango.descripcion() + " (" + rango.inicio().getDayOfWeek() + ").");
        }
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public List<Franja> getDisponibilidad() { return Collections.unmodifiableList(disponibilidad); }
}
