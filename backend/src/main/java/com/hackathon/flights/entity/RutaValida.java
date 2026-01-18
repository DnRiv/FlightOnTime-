package com.hackathon.flights.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "rutas_validas")
public class RutaValida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aerolinea", nullable = false, length = 2)
    private String aerolinea;

    @Column(name = "origen", nullable = false, length = 3)
    private String origen;

    @Column(name = "destino", nullable = false, length = 3)
    private String destino;

    // Constructor vacío
    public RutaValida() {}

    public RutaValida(String aerolinea, String origen, String destino) {
        this.aerolinea = aerolinea != null ? aerolinea.trim().toUpperCase() : null;
        this.origen = origen != null ? origen.trim().toUpperCase() : null;
        this.destino = destino != null ? destino.trim().toUpperCase() : null;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getAerolinea() {
        return aerolinea;
    }

    public String getOrigen() {
        return origen;
    }

    public String getDestino() {
        return destino;
    }
}