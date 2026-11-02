package com.sistematurnos.agenda.contract;

import com.sistematurnos.agenda.application.port.out.ProfesionalRepositoryPort;
import com.sistematurnos.agenda.testsupport.ProfesionalRepositoryEnMemoria;

class ProfesionalRepositoryEnMemoriaContractTest extends ProfesionalRepositoryContractTest {
    private final ProfesionalRepositoryEnMemoria repo = new ProfesionalRepositoryEnMemoria();
    @Override protected ProfesionalRepositoryPort repo() { return repo; }
}
