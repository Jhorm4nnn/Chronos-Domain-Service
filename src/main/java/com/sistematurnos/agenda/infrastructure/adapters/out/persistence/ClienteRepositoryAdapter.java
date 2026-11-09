package com.sistematurnos.agenda.infrastructure.adapters.out.persistence;

import com.sistematurnos.agenda.application.port.out.ClienteRepositoryPort;
import com.sistematurnos.agenda.domain.model.Cliente;
import com.sistematurnos.agenda.domain.vo.Telefono;
import com.sistematurnos.agenda.infrastructure.adapters.out.persistence.entity.ClienteEntity;
import com.sistematurnos.agenda.infrastructure.adapters.out.persistence.mapper.ClienteMapper;
import com.sistematurnos.agenda.infrastructure.adapters.out.persistence.repository.ClienteJpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class ClienteRepositoryAdapter implements ClienteRepositoryPort {

    private final ClienteJpaRepository jpa;

    public ClienteRepositoryAdapter(ClienteJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Cliente guardar(Cliente cliente) {
        // Si ya existe se reutiliza la entidad cargada para conservar su @Version.
        ClienteEntity entidad = cliente.getId() == null
                ? ClienteEntity.nueva()
                : jpa.findById(cliente.getId()).orElseGet(ClienteEntity::nueva);
        ClienteMapper.copiarA(entidad, cliente);
        return ClienteMapper.aDominio(jpa.saveAndFlush(entidad));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cliente> buscarPorId(long id) {
        return jpa.findById(id).map(ClienteMapper::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cliente> buscarPorTelefono(Telefono telefono) {
        return jpa.findByTelefono(telefono.valor()).map(ClienteMapper::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> buscarPorIds(Collection<Long> ids) {
        return jpa.findAllById(ids).stream().map(ClienteMapper::aDominio).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> listar() {
        return jpa.findAllByOrderByIdAsc().stream().map(ClienteMapper::aDominio).toList();
    }
}
