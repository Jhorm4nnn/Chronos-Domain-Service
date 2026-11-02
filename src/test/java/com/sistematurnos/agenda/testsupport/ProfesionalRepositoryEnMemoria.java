package com.sistematurnos.agenda.testsupport;

import com.sistematurnos.agenda.application.port.out.ProfesionalRepositoryPort;
import com.sistematurnos.agenda.domain.model.Profesional;
import java.util.*;

public class ProfesionalRepositoryEnMemoria implements ProfesionalRepositoryPort {

    private final Map<Long, Profesional> filas = new TreeMap<>();
    private long siguiente = 1;

    @Override
    public Profesional guardar(Profesional p) {
        long id = p.getId() == null ? siguiente++ : p.getId();
        Profesional copia = Profesional.reconstruir(id, p.getNombre(), p.getDisponibilidad());
        filas.put(id, copia);
        return copia;
    }

    @Override
    public Optional<Profesional> buscarPorId(long id) { return Optional.ofNullable(filas.get(id)); }

    @Override
    public Optional<Profesional> buscarPorNombre(String nombre) {
        return filas.values().stream().filter(p -> p.getNombre().equalsIgnoreCase(nombre.trim())).findFirst();
    }

    @Override
    public List<Profesional> buscarPorIds(Collection<Long> ids) {
        return filas.values().stream().filter(p -> ids.contains(p.getId())).toList();
    }

    /** En memoria no hay concurrencia real: basta con buscarlo. */
    @Override
    public Optional<Profesional> buscarParaAgendar(long id) { return buscarPorId(id); }

    @Override
    public List<Profesional> listar() { return new ArrayList<>(filas.values()); }
}
