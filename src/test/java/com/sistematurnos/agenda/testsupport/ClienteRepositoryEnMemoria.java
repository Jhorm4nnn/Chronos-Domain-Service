package com.sistematurnos.agenda.testsupport;

import com.sistematurnos.agenda.application.port.out.ClienteRepositoryPort;
import com.sistematurnos.agenda.domain.model.Cliente;
import com.sistematurnos.agenda.domain.vo.Telefono;
import java.util.*;

/** Doble de prueba: guarda copias (como una base de datos real), así un objeto modificado no cambia lo ya guardado. */
public class ClienteRepositoryEnMemoria implements ClienteRepositoryPort {

    private final Map<Long, Cliente> filas = new TreeMap<>();
    private long siguiente = 1;

    @Override
    public Cliente guardar(Cliente c) {
        long id = c.getId() == null ? siguiente++ : c.getId();
        Cliente copia = Cliente.reconstruir(id, c.getNombre(), c.getTelefono(), c.getCorreo());
        filas.put(id, copia);
        return copia;
    }

    @Override
    public Optional<Cliente> buscarPorId(long id) { return Optional.ofNullable(filas.get(id)); }

    @Override
    public Optional<Cliente> buscarPorTelefono(Telefono t) {
        return filas.values().stream().filter(c -> c.getTelefono().equals(t)).findFirst();
    }

    @Override
    public List<Cliente> buscarPorIds(Collection<Long> ids) {
        return filas.values().stream().filter(c -> ids.contains(c.getId())).toList();
    }

    @Override
    public List<Cliente> listar() { return new ArrayList<>(filas.values()); }
}
