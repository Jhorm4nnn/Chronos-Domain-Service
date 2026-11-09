package com.sistematurnos.agenda.infrastructure.adapters.out.persistence.mapper;

import com.sistematurnos.agenda.domain.model.Cliente;
import com.sistematurnos.agenda.domain.vo.Telefono;
import com.sistematurnos.agenda.infrastructure.adapters.out.persistence.entity.ClienteEntity;

/** Traduce entre el modelo de dominio y el de base de datos. Ninguno de los dos conoce al otro. */
public final class ClienteMapper {

    private ClienteMapper() { }

    public static Cliente aDominio(ClienteEntity e) {
        return Cliente.reconstruir(e.getId(), e.getNombre(), new Telefono(e.getTelefono()), e.getCorreo());
    }

    /** Copia el estado del dominio sobre una entidad (nueva o ya cargada, para conservar su @Version). */
    public static void copiarA(ClienteEntity e, Cliente c) {
        e.setNombre(c.getNombre());
        e.setTelefono(c.getTelefono().valor());
        e.setCorreo(c.getCorreo());
    }
}
