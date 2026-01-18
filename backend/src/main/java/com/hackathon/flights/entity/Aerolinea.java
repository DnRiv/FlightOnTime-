package com.hackathon.flights.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "aerolineas")
public class Aerolinea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 2)
    private String codigo;

    // Constructor vacío (requerido por JPA)
    public Aerolinea() {}

    public Aerolinea(String codigo) {
        this.codigo = codigo != null ? codigo.trim().toUpperCase() : null;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    // Setters (opcional, pero buena práctica)
    public void setCodigo(String codigo) {
        this.codigo = codigo != null ? codigo.trim().toUpperCase() : null;
    }
}

