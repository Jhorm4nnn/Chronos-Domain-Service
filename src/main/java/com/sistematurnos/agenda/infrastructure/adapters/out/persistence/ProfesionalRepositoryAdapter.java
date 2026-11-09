package com.sistematurnos.agenda.infrastructure.adapters.out.persistence;

import com.sistematurnos.agenda.application.port.out.ProfesionalRepositoryPort;
import com.sistematurnos.agenda.domain.model.Profesional;
import com.sistematurnos.agenda.infrastructure.adapters.out.persistence.entity.ProfesionalEntity;
import com.sistematurnos.agenda.infrastructure.adapters.out.persistence.mapper.ProfesionalMapper;
import com.sistematurnos.agenda.infrastructure.adapters.out.persistence.repository.ProfesionalJpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class ProfesionalRepositoryAdapter implements ProfesionalRepositoryPort {

    private final ProfesionalJpaRepository jpa;

    public ProfesionalRepositoryAdapter(ProfesionalJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Profesional guardar(Profesional profesional) {
        ProfesionalEntity entidad = profesional.getId() == null
                ? ProfesionalEntity.nueva()
                : jpa.findById(profesional.getId()).orElseGet(ProfesionalEntity::nueva);
        ProfesionalMapper.copiarA(entidad, profesional);
        return ProfesionalMapper.aDominio(jpa.saveAndFlush(entidad));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Profesional> buscarPorId(long id) {
        return jpa.findById(id).map(ProfesionalMapper::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Profesional> buscarPorNombre(String nombre) {
        return jpa.findByNombreClave(ProfesionalMapper.clave(nombre)).map(ProfesionalMapper::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Profesional> buscarPorIds(Collection<Long> ids) {
        return jpa.findAllById(ids).stream().map(ProfesionalMapper::aDominio).toList();
    }

    /** Lectura con SELECT ... FOR UPDATE; dura hasta que termina la transacción del caso de uso que la pidió. */
    @Override
    public Optional<Profesional> buscarParaAgendar(long id) {
        return jpa.buscarBloqueando(id).map(ProfesionalMapper::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Profesional> listar() {
        return jpa.findAllByOrderByIdAsc().stream().map(ProfesionalMapper::aDominio).toList();
    }
}
