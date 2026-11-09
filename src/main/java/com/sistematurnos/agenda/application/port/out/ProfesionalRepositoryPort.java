package com.sistematurnos.agenda.application.port.out;

import com.sistematurnos.agenda.domain.model.Profesional;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProfesionalRepositoryPort {
    Profesional guardar(Profesional profesional);
    Optional<Profesional> buscarPorId(long id);
    /** Compara sin distinguir mayúsculas. */
    Optional<Profesional> buscarPorNombre(String nombre);
    List<Profesional> buscarPorIds(Collection<Long> ids);
    /**
     * Igual que buscarPorId, pero mantiene bloqueado al profesional hasta que termine la transacción: así dos agendas
     * simultáneas del mismo profesional se procesan una tras otra y no pueden crear dos turnos que se crucen.
     */
    Optional<Profesional> buscarParaAgendar(long id);
    /** Ordenados por id ascendente. */
    List<Profesional> listar();
}
