package com.sistematurnos.agenda.application.port.out;

import com.sistematurnos.agenda.domain.model.Cliente;
import com.sistematurnos.agenda.domain.vo.Telefono;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ClienteRepositoryPort {
    /** Con id nulo inserta y asigna id; con id existente actualiza. */
    Cliente guardar(Cliente cliente);
    Optional<Cliente> buscarPorId(long id);
    Optional<Cliente> buscarPorTelefono(Telefono telefono);
    List<Cliente> buscarPorIds(Collection<Long> ids);
    /** Ordenados por id ascendente. */
    List<Cliente> listar();
}
