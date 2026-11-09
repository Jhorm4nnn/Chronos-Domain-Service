package com.sistematurnos.agenda.infrastructure.adapters.out.persistence;

import com.sistematurnos.agenda.application.port.out.ClienteRepositoryPort;
import com.sistematurnos.agenda.contract.ClienteRepositoryContractTest;
import com.sistematurnos.agenda.domain.model.Cliente;
import com.sistematurnos.agenda.domain.vo.Telefono;
import jakarta.persistence.EntityManager;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

/** El adaptador JPA pasa EXACTAMENTE las mismas pruebas que el doble en memoria (Liskov). */
@DataJpaTest
@Import(ClienteRepositoryAdapter.class)
class ClienteRepositoryAdapterContractTest extends ClienteRepositoryContractTest {

    @Autowired
    private ClienteRepositoryAdapter adaptador;
    @Autowired
    private EntityManager em;

    /** Después de cada guardar se vacía la caché de Hibernate: así las lecturas van de verdad a la base de datos. */
    @Override
    protected ClienteRepositoryPort repo() {
        return new ClienteRepositoryPort() {
            public Cliente guardar(Cliente c) { Cliente r = adaptador.guardar(c); em.flush(); em.clear(); return r; }
            public Optional<Cliente> buscarPorId(long id) { return adaptador.buscarPorId(id); }
            public Optional<Cliente> buscarPorTelefono(Telefono t) { return adaptador.buscarPorTelefono(t); }
            public List<Cliente> buscarPorIds(Collection<Long> ids) { return adaptador.buscarPorIds(ids); }
            public List<Cliente> listar() { return adaptador.listar(); }
        };
    }
}
