package com.sistematurnos.agenda.contract;

import com.sistematurnos.agenda.application.port.out.TurnoRepositoryPort;
import com.sistematurnos.agenda.testsupport.TurnoRepositoryEnMemoria;

class TurnoRepositoryEnMemoriaContractTest extends TurnoRepositoryContractTest {
    private final TurnoRepositoryEnMemoria repo = new TurnoRepositoryEnMemoria();
    private long seq = 0;
    @Override protected TurnoRepositoryPort repo() { return repo; }
    @Override protected long crearCliente(String telefono) { return ++seq; }
    @Override protected long crearProfesional(String nombre) { return ++seq; }
}
