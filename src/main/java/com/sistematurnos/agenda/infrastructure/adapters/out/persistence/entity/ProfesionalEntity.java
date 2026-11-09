package com.sistematurnos.agenda.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "profesional", uniqueConstraints = @UniqueConstraint(name = "uk_profesional_nombre", columnNames = "nombre_clave"))
public class ProfesionalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    /** El nombre en minúsculas: así UNIQUE no distingue mayúsculas. */
    @Column(name = "nombre_clave", nullable = false, length = 120)
    private String nombreClave;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profesional_franja", joinColumns = @JoinColumn(name = "profesional_id"))
    @OrderColumn(name = "orden")
    private List<FranjaEmbeddable> disponibilidad = new ArrayList<>();

    @Version
    private Long version;

    protected ProfesionalEntity() { }

    public static ProfesionalEntity nueva() { return new ProfesionalEntity(); }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getNombreClave() { return nombreClave; }
    public void setNombreClave(String nombreClave) { this.nombreClave = nombreClave; }
    public List<FranjaEmbeddable> getDisponibilidad() { return disponibilidad; }
}
