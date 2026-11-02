package com.sistematurnos.agenda.infrastructure.config;

import com.sistematurnos.agenda.application.port.in.ClienteUseCase;
import com.sistematurnos.agenda.application.port.in.ProfesionalUseCase;
import com.sistematurnos.agenda.application.port.in.TurnoUseCase;
import com.sistematurnos.agenda.infrastructure.adapters.in.cli.TurnosCli;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Con el perfil «cli» la aplicación arranca sin servidor web y atiende comandos por la consola. */
@Configuration
@Profile("cli")
public class CliConfig {

    @Bean
    CommandLineRunner consola(ClienteUseCase clientes, ProfesionalUseCase profesionales, TurnoUseCase turnos) {
        return args -> new TurnosCli(clientes, profesionales, turnos, System.in, System.out).ejecutar();
    }
}
