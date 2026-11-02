package com.sistematurnos.agenda.application.port.in;

import com.sistematurnos.agenda.application.port.in.command.RegistrarProfesionalCommand;
import com.sistematurnos.agenda.domain.model.Profesional;
import java.util.List;

public interface ProfesionalUseCase {
    Profesional registrar(RegistrarProfesionalCommand comando);
    Profesional porId(long id);
    List<Profesional> listar();
}
