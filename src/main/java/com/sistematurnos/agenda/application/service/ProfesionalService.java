package com.sistematurnos.agenda.application.service;

import com.sistematurnos.agenda.application.port.in.ProfesionalUseCase;
import com.sistematurnos.agenda.application.port.in.command.RegistrarProfesionalCommand;
import com.sistematurnos.agenda.application.port.out.ProfesionalRepositoryPort;
import com.sistematurnos.agenda.domain.exception.ProfesionalDuplicadoException;
import com.sistematurnos.agenda.domain.exception.ProfesionalNoEncontradoException;
import com.sistematurnos.agenda.domain.exception.ValorInvalidoException;
import com.sistematurnos.agenda.domain.model.Profesional;
import com.sistematurnos.agenda.domain.vo.Franja;
import java.util.ArrayList;
import java.util.List;

public class ProfesionalService implements ProfesionalUseCase {

    private final ProfesionalRepositoryPort profesionales;

    public ProfesionalService(ProfesionalRepositoryPort profesionales) {
        this.profesionales = profesionales;
    }

    @Override
    public Profesional registrar(RegistrarProfesionalCommand c) {
        List<String> faltantes = new ArrayList<>();
        if (c.nombre() == null || c.nombre().isBlank()) faltantes.add("nombre");
        if (c.disponibilidad() == null || c.disponibilidad().isEmpty()) faltantes.add("disponibilidad");
        if (!faltantes.isEmpty()) {
            throw new ValorInvalidoException("Faltan campos obligatorios: " + String.join(", ", faltantes));
        }
        List<Franja> franjas = c.disponibilidad().stream()
                .map(f -> new Franja(f.dia(), f.desde(), f.hasta()))
                .toList();
        Profesional nuevo = Profesional.registrar(c.nombre(), franjas);
        if (profesionales.buscarPorNombre(nuevo.getNombre()).isPresent()) {
            throw new ProfesionalDuplicadoException(nuevo.getNombre());
        }
        return profesionales.guardar(nuevo);
    }

    @Override
    public Profesional porId(long id) {
        return profesionales.buscarPorId(id).orElseThrow(() -> new ProfesionalNoEncontradoException(id));
    }

    @Override
    public List<Profesional> listar() {
        return profesionales.listar();
    }
}
