package com.sistematurnos.agenda.infrastructure.config;

import com.sistematurnos.agenda.application.port.out.*;
import com.sistematurnos.agenda.application.service.*;
import com.sistematurnos.agenda.domain.service.ValidadorAgenda;
import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Raíz de composición. El dominio y la aplicación no llevan anotaciones de Spring (Semana 5, DIP):
 * es esta clase, en el borde, la que los construye y les entrega sus dependencias.
 * Los dos adaptadores de entrada (REST y CLI) reciben ESTOS mismos servicios.
 */
@Configuration
public class BeanConfig {

    @Bean
    Clock reloj(@Value("${negocio.zona-horaria}") String zona) {
        return Clock.system(ZoneId.of(zona));
    }

    @Bean
    ValidadorAgenda validadorAgenda() {
        return new ValidadorAgenda();
    }

    @Bean
    ClienteService clienteService(ClienteRepositoryPort clientes) {
        return new ClienteService(clientes);
    }

    @Bean
    ProfesionalService profesionalService(ProfesionalRepositoryPort profesionales) {
        return new ProfesionalService(profesionales);
    }

    @Bean
    TurnoService turnoService(ClienteRepositoryPort clientes, ProfesionalRepositoryPort profesionales, TurnoRepositoryPort turnos,
                              TransaccionPort transaccion, Clock reloj, ValidadorAgenda validador) {
        return new TurnoService(clientes, profesionales, turnos, transaccion, reloj, validador);
    }
}
