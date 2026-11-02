package com.sistematurnos.agenda.infrastructure.adapters.in.rest;

import com.sistematurnos.agenda.application.port.in.TurnoUseCase;
import com.sistematurnos.agenda.application.port.in.command.AgendarTurnoCommand;
import com.sistematurnos.agenda.application.port.in.command.ConsultarTurnosQuery;
import com.sistematurnos.agenda.application.port.in.command.ReprogramarTurnoCommand;
import com.sistematurnos.agenda.infrastructure.adapters.in.rest.dto.ReprogramacionRequest;
import com.sistematurnos.agenda.infrastructure.adapters.in.rest.dto.TurnoRequest;
import com.sistematurnos.agenda.infrastructure.adapters.in.rest.dto.TurnoResponse;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** HU-9 a HU-12: crear, cancelar, reprogramar y consultar turnos. */
@RestController
@RequestMapping("/api/turnos")
public class TurnoController {

    private final TurnoUseCase turnos;

    public TurnoController(TurnoUseCase turnos) {
        this.turnos = turnos;
    }

    @PostMapping
    public ResponseEntity<TurnoResponse> agendar(@RequestBody TurnoRequest r) {
        var creado = turnos.agendar(new AgendarTurnoCommand(r.clienteId(), r.profesionalId(), r.fecha(), r.hora(), r.duracionMinutos()));
        return ResponseEntity.created(URI.create("/api/turnos/" + creado.turno().getId())).body(TurnoResponse.de(creado));
    }

    @GetMapping
    public List<TurnoResponse> consultar(@RequestParam(required = false) Long profesionalId,
                                         @RequestParam(required = false) Long clienteId,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                         @RequestParam(defaultValue = "false") boolean incluirCancelados) {
        return turnos.consultar(new ConsultarTurnosQuery(profesionalId, clienteId, desde, hasta, incluirCancelados))
                .stream().map(TurnoResponse::de).toList();
    }

    @GetMapping("/{id}")
    public TurnoResponse obtener(@PathVariable long id) {
        return TurnoResponse.de(turnos.porId(id));
    }

    /** Cancelar es lógico: el turno queda CANCELADO y se conserva su historial. */
    @PostMapping("/{id}/cancelacion")
    public TurnoResponse cancelar(@PathVariable long id) {
        return TurnoResponse.de(turnos.cancelar(id));
    }

    @PutMapping("/{id}/horario")
    public TurnoResponse reprogramar(@PathVariable long id, @RequestBody ReprogramacionRequest r) {
        return TurnoResponse.de(turnos.reprogramar(new ReprogramarTurnoCommand(id, r.fecha(), r.hora(), r.duracionMinutos())));
    }
}
