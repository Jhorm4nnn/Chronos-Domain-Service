package com.sistematurnos.agenda.infrastructure.adapters.out.persistence;

import com.sistematurnos.agenda.application.port.out.FiltroTurno;
import com.sistematurnos.agenda.application.port.out.TurnoRepositoryPort;
import com.sistematurnos.agenda.contract.TurnoRepositoryContractTest;
import com.sistematurnos.agenda.domain.model.Turno;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TurnoRepositoryAdapter.class)
class TurnoRepositoryAdapterContractTest extends TurnoRepositoryContractTest {

    @Autowired
    private TurnoRepositoryAdapter adaptador;
    @Autowired
    private EntityManager em;

    private long seq = 100;

    @Override
    protected TurnoRepositoryPort repo() {
        return new TurnoRepositoryPort() {
            public Turno guardar(Turno t) { Turno r = adaptador.guardar(t); em.flush(); em.clear(); return r; }
            public Optional<Turno> buscarPorId(long id) { return adaptador.buscarPorId(id); }
            public List<Turno> buscarAgendadosDelProfesional(long p, LocalDate f) { return adaptador.buscarAgendadosDelProfesional(p, f); }
            public List<Turno> buscar(FiltroTurno f) { return adaptador.buscar(f); }
        };
    }

    /** El turno enlaza por id (sin llave foránea) con su cliente y su profesional: basta con ids nuevos. */
    @Override
    protected long crearCliente(String telefono) { return ++seq; }

    @Override
    protected long crearProfesional(String nombre) { return ++seq; }
}
