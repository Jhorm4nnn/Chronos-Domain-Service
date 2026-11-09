package com.sistematurnos.agenda.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalTime;

@Embeddable
public class FranjaEmbeddable {

    @Column(nullable = false, length = 10)
    private String dia;

    @Column(nullable = false)
    private LocalTime desde;

    @Column(nullable = false)
    private LocalTime hasta;

    protected FranjaEmbeddable() { }

    public FranjaEmbeddable(String dia, LocalTime desde, LocalTime hasta) {
        this.dia = dia;
        this.desde = desde;
        this.hasta = hasta;
    }

    public String getDia() { return dia; }
    public LocalTime getDesde() { return desde; }
    public LocalTime getHasta() { return hasta; }
}
