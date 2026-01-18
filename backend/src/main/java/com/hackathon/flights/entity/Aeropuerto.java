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

    public Aeropuerto() {}

    public Aeropuerto(String codigoIata) {
        this.codigoIata = codigoIata != null ? codigoIata.trim().toUpperCase() : null;
    }

    public String getCodigoIata() {
        return codigoIata;
    }
}
