package com.sistematurnos.agenda.infrastructure.adapters.in.rest;

import com.sistematurnos.agenda.application.port.in.ProfesionalUseCase;
import com.sistematurnos.agenda.application.port.in.command.RegistrarProfesionalCommand;
import com.sistematurnos.agenda.application.port.in.command.RegistrarProfesionalCommand.FranjaCommand;
import com.sistematurnos.agenda.infrastructure.adapters.in.rest.dto.ProfesionalRequest;
import com.sistematurnos.agenda.infrastructure.adapters.in.rest.dto.ProfesionalResponse;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** HU-8: profesionales con su horario de disponibilidad. */
@RestController
@RequestMapping("/api/profesionales")
public class ProfesionalController {

    private final ProfesionalUseCase profesionales;

    public ProfesionalController(ProfesionalUseCase profesionales) {
        this.profesionales = profesionales;
    }

    @PostMapping
    public ResponseEntity<ProfesionalResponse> registrar(@RequestBody ProfesionalRequest r) {
        List<FranjaCommand> franjas = r.disponibilidad() == null ? null
                : r.disponibilidad().stream().map(f -> new FranjaCommand(f.dia(), f.desde(), f.hasta())).toList();
        var creado = profesionales.registrar(new RegistrarProfesionalCommand(r.nombre(), franjas));
        return ResponseEntity.created(URI.create("/api/profesionales/" + creado.getId())).body(ProfesionalResponse.de(creado));
    }

    @GetMapping
    public List<ProfesionalResponse> listar() {
        return profesionales.listar().stream().map(ProfesionalResponse::de).toList();
    }

    @GetMapping("/{id}")
    public ProfesionalResponse obtener(@PathVariable long id) {
        return ProfesionalResponse.de(profesionales.porId(id));
    }
}
