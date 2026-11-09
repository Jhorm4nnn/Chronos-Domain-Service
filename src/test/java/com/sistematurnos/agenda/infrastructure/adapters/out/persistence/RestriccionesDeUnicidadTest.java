package com.sistematurnos.agenda.infrastructure.adapters.out.persistence;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sistematurnos.agenda.domain.model.Cliente;
import com.sistematurnos.agenda.domain.model.Profesional;
import com.sistematurnos.agenda.domain.vo.Telefono;
import com.sistematurnos.agenda.testsupport.Datos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

/** La base de datos es la última defensa cuando dos registros iguales llegan al mismo tiempo. */
@DataJpaTest
@Import({ClienteRepositoryAdapter.class, ProfesionalRepositoryAdapter.class})
class RestriccionesDeUnicidadTest {

    @Autowired
    private ClienteRepositoryAdapter clientes;
    @Autowired
    private ProfesionalRepositoryAdapter profesionales;

    @Test
    @DisplayName("Dos clientes con el mismo teléfono: la base de datos lo rechaza")
    void telefonoUnico() {
        clientes.guardar(Datos.ana());
        assertThrows(DataIntegrityViolationException.class,
                () -> clientes.guardar(Cliente.registrar("Otra Ana", Telefono.de("3001234567"), null)));
    }

    @Test
    @DisplayName("Dos profesionales con el mismo nombre (aunque cambien las mayúsculas): la base de datos lo rechaza")
    void nombreUnico() {
        profesionales.guardar(Datos.doctora());
        assertThrows(DataIntegrityViolationException.class,
                () -> profesionales.guardar(Profesional.registrar("DRA. LÓPEZ", Datos.semanaLaboral())));
    }
}
