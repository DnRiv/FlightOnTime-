package com.hackathon.flights.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "aeropuertos_zonas")
public class AeropuertoZona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_iata", nullable = false, unique = true, length = 3)
    private String codigoIata;

    @Column(name = "zona_horaria", nullable = false, length = 50)
    private String zonaHoraria;

    // Constructor vacío
    public AeropuertoZona() {}

    public AeropuertoZona(String codigoIata, String zonaHoraria) {
        this.codigoIata = codigoIata != null ? codigoIata.trim().toUpperCase() : null;
        this.zonaHoraria = zonaHoraria;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getCodigoIata() {
        return codigoIata;
    }

    public String getZonaHoraria() {
        return zonaHoraria;
    }
}

