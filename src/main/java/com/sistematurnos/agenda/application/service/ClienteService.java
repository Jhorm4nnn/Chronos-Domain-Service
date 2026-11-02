package com.sistematurnos.agenda.application.service;

import com.sistematurnos.agenda.application.port.in.ClienteUseCase;
import com.sistematurnos.agenda.application.port.in.command.RegistrarClienteCommand;
import com.sistematurnos.agenda.application.port.out.ClienteRepositoryPort;
import com.sistematurnos.agenda.domain.exception.ClienteDuplicadoException;
import com.sistematurnos.agenda.domain.exception.ClienteNoEncontradoException;
import com.sistematurnos.agenda.domain.exception.ValorInvalidoException;
import com.sistematurnos.agenda.domain.model.Cliente;
import com.sistematurnos.agenda.domain.vo.Telefono;
import java.util.ArrayList;
import java.util.List;

public class ClienteService implements ClienteUseCase {

    private final ClienteRepositoryPort clientes;

    public ClienteService(ClienteRepositoryPort clientes) {
        this.clientes = clientes;
    }

    @Override
    public Cliente registrar(RegistrarClienteCommand c) {
        List<String> faltantes = new ArrayList<>();
        if (c.nombre() == null || c.nombre().isBlank()) faltantes.add("nombre");
        if (c.telefono() == null || c.telefono().isBlank()) faltantes.add("teléfono");
        if (!faltantes.isEmpty()) {
            throw new ValorInvalidoException("Faltan campos obligatorios: " + String.join(", ", faltantes));
        }
        Cliente nuevo = Cliente.registrar(c.nombre(), Telefono.de(c.telefono()), c.correo());
        if (clientes.buscarPorTelefono(nuevo.getTelefono()).isPresent()) {
            throw new ClienteDuplicadoException(nuevo.getTelefono().valor());
        }
        return clientes.guardar(nuevo);
    }

    @Override
    public Cliente porId(long id) {
        return clientes.buscarPorId(id).orElseThrow(() -> new ClienteNoEncontradoException(id));
    }

    @Override
    public List<Cliente> listar() {
        return clientes.listar();
    }
}
