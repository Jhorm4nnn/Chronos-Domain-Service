package com.sistematurnos.agenda.infrastructure.adapters.out.persistence.repository;

import com.sistematurnos.agenda.infrastructure.adapters.out.persistence.entity.ProfesionalEntity;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProfesionalJpaRepository extends JpaRepository<ProfesionalEntity, Long> {
    Optional<ProfesionalEntity> findByNombreClave(String nombreClave);
    List<ProfesionalEntity> findAllByOrderByIdAsc();

    /** SELECT ... FOR UPDATE: mientras dure la transacción nadie más puede agendar con este profesional. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProfesionalEntity p where p.id = :id")
    Optional<ProfesionalEntity> buscarBloqueando(@Param("id") long id);
}
