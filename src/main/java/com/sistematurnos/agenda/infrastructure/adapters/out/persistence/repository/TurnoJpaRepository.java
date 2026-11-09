package com.sistematurnos.agenda.infrastructure.adapters.out.persistence.repository;

import com.sistematurnos.agenda.infrastructure.adapters.out.persistence.entity.TurnoEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TurnoJpaRepository extends JpaRepository<TurnoEntity, Long>, JpaSpecificationExecutor<TurnoEntity> {

    @Query("select t from TurnoEntity t where t.profesionalId = :profesionalId and t.estado = 'AGENDADO' "
            + "and t.inicio >= :desde and t.inicio < :hasta order by t.inicio")
    List<TurnoEntity> agendadosEntre(@Param("profesionalId") long profesionalId,
                                     @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
