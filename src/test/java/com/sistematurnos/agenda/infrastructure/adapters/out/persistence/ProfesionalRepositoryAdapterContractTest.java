package com.sistematurnos.agenda.infrastructure.adapters.out.persistence;

import com.sistematurnos.agenda.application.port.out.ProfesionalRepositoryPort;
import com.sistematurnos.agenda.contract.ProfesionalRepositoryContractTest;
import com.sistematurnos.agenda.domain.model.Profesional;
import jakarta.persistence.EntityManager;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

@DataJpaTest
@Import(ProfesionalRepositoryAdapter.class)
class ProfesionalRepositoryAdapterContractTest extends ProfesionalRepositoryContractTest {

    @Autowired
    private ProfesionalRepositoryAdapter adaptador;
    @Autowired
    private EntityManager em;

    @Autowired
    private PlatformTransactionManager transacciones;
    private TransactionStatus transaccion;

    // @DataJpaTest solo envuelve en transacción los @Test declarados en esta clase, no los heredados del contrato.
    // Se abre aquí y se revierte al terminar, para que cada prueba empiece con la base de datos vacía.
    @BeforeEach
    void abrirTransaccion() {
        transaccion = transacciones.getTransaction(TransactionDefinition.withDefaults());
    }

    @AfterEach
    void revertirTransaccion() {
        transacciones.rollback(transaccion);
    }

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
