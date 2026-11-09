package com.sistematurnos.agenda.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "turno", indexes = @Index(name = "ix_turno_profesional_inicio", columnList = "profesional_id, inicio"))
public class TurnoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private long clienteId;

    @Column(name = "profesional_id", nullable = false)
    private long profesionalId;

    @Column(nullable = false)
    private LocalDateTime inicio;

    @Column(nullable = false)
    private LocalDateTime fin;

    @Column(nullable = false, length = 12)
    private String estado;

    @Version
    private Long version;

    protected TurnoEntity() { }

    public static TurnoEntity nueva() { return new TurnoEntity(); }

    public Long getId() { return id; }
    public long getClienteId() { return clienteId; }
    public void setClienteId(long clienteId) { this.clienteId = clienteId; }
    public long getProfesionalId() { return profesionalId; }
    public void setProfesionalId(long profesionalId) { this.profesionalId = profesionalId; }
    public LocalDateTime getInicio() { return inicio; }
    public void setInicio(LocalDateTime inicio) { this.inicio = inicio; }
    public LocalDateTime getFin() { return fin; }
    public void setFin(LocalDateTime fin) { this.fin = fin; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
