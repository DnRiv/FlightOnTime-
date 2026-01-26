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

    @Column(name = "nombre", length = 50)
    private String nombre;

    // Constructor vacío (requerido por JPA)
    public Aerolinea() {}

    // Constructor solo con código (para compatibilidad)
    public Aerolinea(String codigo) {
        this(codigo, null); // delega al constructor completo
    }

    // ✅ Constructor completo
    public Aerolinea(String codigo, String nombre) {
        this.codigo = codigo != null ? codigo.trim().toUpperCase() : null;
        this.nombre = nombre;
    }

    // Getters
    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }

    // Setters
    public void setCodigo(String codigo) {
        this.codigo = codigo != null ? codigo.trim().toUpperCase() : null;
    }
    public void setNombre(String nombre) { this.nombre = nombre; }
}

