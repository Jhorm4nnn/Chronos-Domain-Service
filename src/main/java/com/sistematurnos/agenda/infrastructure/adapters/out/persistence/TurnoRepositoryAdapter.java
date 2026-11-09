package com.sistematurnos.agenda.infrastructure.adapters.out.persistence;

import com.sistematurnos.agenda.application.port.out.FiltroTurno;
import com.sistematurnos.agenda.application.port.out.TurnoRepositoryPort;
import com.sistematurnos.agenda.domain.model.Turno;
import com.sistematurnos.agenda.infrastructure.adapters.out.persistence.entity.TurnoEntity;
import com.sistematurnos.agenda.infrastructure.adapters.out.persistence.mapper.TurnoMapper;
import com.sistematurnos.agenda.infrastructure.adapters.out.persistence.repository.TurnoJpaRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class TurnoRepositoryAdapter implements TurnoRepositoryPort {

    private final TurnoJpaRepository jpa;

    public TurnoRepositoryAdapter(TurnoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Turno guardar(Turno turno) {
        TurnoEntity entidad = turno.getId() == null
                ? TurnoEntity.nueva()
                : jpa.findById(turno.getId()).orElseGet(TurnoEntity::nueva);
        TurnoMapper.copiarA(entidad, turno);
        return TurnoMapper.aDominio(jpa.saveAndFlush(entidad));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Turno> buscarPorId(long id) {
        return jpa.findById(id).map(TurnoMapper::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Turno> buscarAgendadosDelProfesional(long profesionalId, LocalDate fecha) {
        return jpa.agendadosEntre(profesionalId, fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay())
                .stream().map(TurnoMapper::aDominio).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Turno> buscar(FiltroTurno f) {
        Specification<TurnoEntity> spec = (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            if (f.profesionalId() != null) {
                condiciones.add(cb.equal(root.get("profesionalId"), f.profesionalId()));
            }
            if (f.clienteId() != null) {
                condiciones.add(cb.equal(root.get("clienteId"), f.clienteId()));
            }
            if (f.desde() != null) {
                condiciones.add(cb.greaterThanOrEqualTo(root.get("inicio"), f.desde().atStartOfDay()));
            }
            if (f.hasta() != null) {
                condiciones.add(cb.lessThan(root.get("inicio"), f.hasta().plusDays(1).atStartOfDay()));
            }
            if (!f.incluirCancelados()) {
                condiciones.add(cb.equal(root.get("estado"), "AGENDADO"));
            }
            return cb.and(condiciones.toArray(new Predicate[0]));
        };
        return jpa.findAll(spec, Sort.by("inicio", "id")).stream().map(TurnoMapper::aDominio).toList();
    }
}
