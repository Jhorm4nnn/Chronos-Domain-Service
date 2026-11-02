package com.sistematurnos.agenda.application.port.in;

import com.sistematurnos.agenda.application.port.in.command.AgendarTurnoCommand;
import com.sistematurnos.agenda.application.port.in.command.ConsultarTurnosQuery;
import com.sistematurnos.agenda.application.port.in.command.ReprogramarTurnoCommand;
import com.sistematurnos.agenda.application.port.in.result.TurnoConDatos;
import java.util.List;

public interface TurnoUseCase {
    TurnoConDatos agendar(AgendarTurnoCommand comando);
    TurnoConDatos cancelar(long turnoId);
    TurnoConDatos reprogramar(ReprogramarTurnoCommand comando);
    TurnoConDatos porId(long turnoId);
    /** Ordenados por inicio ascendente. Sin cancelados salvo que se pidan. */
    List<TurnoConDatos> consultar(ConsultarTurnosQuery consulta);
}
