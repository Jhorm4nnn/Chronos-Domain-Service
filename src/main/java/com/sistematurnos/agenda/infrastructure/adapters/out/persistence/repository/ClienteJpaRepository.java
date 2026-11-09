package com.sistematurnos.agenda.infrastructure.adapters.out.persistence.repository;

import com.sistematurnos.agenda.infrastructure.adapters.out.persistence.entity.ClienteEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteJpaRepository extends JpaRepository<ClienteEntity, Long> {
    Optional<ClienteEntity> findByTelefono(String telefono);
    List<ClienteEntity> findAllByOrderByIdAsc();
}
