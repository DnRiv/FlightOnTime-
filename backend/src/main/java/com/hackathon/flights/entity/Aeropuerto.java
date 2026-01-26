package com.hackathon.flights.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "aeropuertos")
public class Aeropuerto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_iata", nullable = false, unique = true, length = 3)
    private String codigoIata;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    // Constructor vacío (requerido por JPA)
    public Aeropuerto() {}

    public Aeropuerto(String codigoIata, String nombre) {
        this.codigoIata = codigoIata != null ? codigoIata.trim().toUpperCase() : null;
        this.nombre = nombre;
    }

    public String getCodigoIata() { return codigoIata; }
    public String getNombre() { return nombre; } // ← AGREGADO
}
