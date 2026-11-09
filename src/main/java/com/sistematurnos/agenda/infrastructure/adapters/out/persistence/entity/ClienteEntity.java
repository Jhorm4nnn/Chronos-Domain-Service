package com.sistematurnos.agenda.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cliente", uniqueConstraints = @UniqueConstraint(name = "uk_cliente_telefono", columnNames = "telefono"))
public class ClienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    /** UNIQUE: la base de datos es la última defensa si dos registros con el mismo teléfono llegan a la vez. */
    @Column(nullable = false, length = 15)
    private String telefono;

    @Column(length = 120)
    private String correo;

    @Version
    private Long version;

    protected ClienteEntity() { }

    public static ClienteEntity nueva() { return new ClienteEntity(); }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
}
