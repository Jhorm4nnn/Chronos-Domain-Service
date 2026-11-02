package com.sistematurnos.agenda.contract;

import com.sistematurnos.agenda.application.port.out.ClienteRepositoryPort;
import com.sistematurnos.agenda.testsupport.ClienteRepositoryEnMemoria;

/** El doble en memoria pasa los mismos contratos que el adaptador JPA: por eso es confiable para probar la aplicación. */
class ClienteRepositoryEnMemoriaContractTest extends ClienteRepositoryContractTest {
    private final ClienteRepositoryEnMemoria repo = new ClienteRepositoryEnMemoria();
    @Override protected ClienteRepositoryPort repo() { return repo; }
}
