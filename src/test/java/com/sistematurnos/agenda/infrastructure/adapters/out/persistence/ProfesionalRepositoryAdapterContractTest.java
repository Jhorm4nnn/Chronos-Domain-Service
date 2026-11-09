package com.sistematurnos.agenda.infrastructure.adapters.out.persistence;

import com.sistematurnos.agenda.application.port.out.ProfesionalRepositoryPort;
import com.sistematurnos.agenda.contract.ProfesionalRepositoryContractTest;
import com.sistematurnos.agenda.domain.model.Profesional;
import jakarta.persistence.EntityManager;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(ProfesionalRepositoryAdapter.class)
class ProfesionalRepositoryAdapterContractTest extends ProfesionalRepositoryContractTest {

    @Autowired
    private ProfesionalRepositoryAdapter adaptador;
    @Autowired
    private EntityManager em;

    @Override
    protected ProfesionalRepositoryPort repo() {
        return new ProfesionalRepositoryPort() {
            public Profesional guardar(Profesional p) { Profesional r = adaptador.guardar(p); em.flush(); em.clear(); return r; }
            public Optional<Profesional> buscarPorId(long id) { return adaptador.buscarPorId(id); }
            public Optional<Profesional> buscarPorNombre(String n) { return adaptador.buscarPorNombre(n); }
            public List<Profesional> buscarPorIds(Collection<Long> ids) { return adaptador.buscarPorIds(ids); }
            public Optional<Profesional> buscarParaAgendar(long id) { return adaptador.buscarParaAgendar(id); }
            public List<Profesional> listar() { return adaptador.listar(); }
        };
    }
}
