package com.sistematurnos.agenda.application.port.in;

import com.sistematurnos.agenda.application.port.in.command.RegistrarClienteCommand;
import com.sistematurnos.agenda.domain.model.Cliente;
import java.util.List;

public interface ClienteUseCase {
    Cliente registrar(RegistrarClienteCommand comando);
    Cliente porId(long id);
    List<Cliente> listar();
}
