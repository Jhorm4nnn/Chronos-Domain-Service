package com.sistematurnos.agenda.infrastructure.adapters.in.rest;

import com.sistematurnos.agenda.application.port.in.ClienteUseCase;
import com.sistematurnos.agenda.application.port.in.command.RegistrarClienteCommand;
import com.sistematurnos.agenda.infrastructure.adapters.in.rest.dto.ClienteRequest;
import com.sistematurnos.agenda.infrastructure.adapters.in.rest.dto.ClienteResponse;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** HU-8: clientes. Primer adaptador de entrada; el segundo es el CLI. */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteUseCase clientes;

    public ClienteController(ClienteUseCase clientes) {
        this.clientes = clientes;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> registrar(@RequestBody ClienteRequest r) {
        var creado = clientes.registrar(new RegistrarClienteCommand(r.nombre(), r.telefono(), r.correo()));
        return ResponseEntity.created(URI.create("/api/clientes/" + creado.getId())).body(ClienteResponse.de(creado));
    }

    @GetMapping
    public List<ClienteResponse> listar() {
        return clientes.listar().stream().map(ClienteResponse::de).toList();
    }

    @GetMapping("/{id}")
    public ClienteResponse obtener(@PathVariable long id) {
        return ClienteResponse.de(clientes.porId(id));
    }
}
