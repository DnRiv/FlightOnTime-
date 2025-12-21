package com.hackathon.flights.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vuelos")
public class Vuelos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2)
    private String aerolinea;

    @Column(nullable = false, length = 3)
    private String origen;

    @Column(nullable = false, length = 3)
    private String destino;

    @Column(name = "fecha_partida")
    private LocalDateTime fechaPartida;

    @Column(name = "distancia")
    private Integer distancia;

    @Column(name = "fecha_consulta", updatable = false)
    private LocalDateTime fechaConsulta = LocalDateTime.now();

    @Column(name = "prevision", length = 20 )
    private String prevision;

    @Column(name = "probabilidad")
    private double probabilidad;

    // Constructores
    public Vuelos() {}

    public Vuelos(String aerolinea, String origen, String destino,
                 LocalDateTime fechaPartida, Integer distancia) {
        this.aerolinea = aerolinea;
        this.origen = origen;
        this.destino = destino;
        this.fechaPartida = fechaPartida;
        this.distancia = distancia;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAerolinea() { return aerolinea; }
    public void setAerolinea(String aerolinea) { this.aerolinea = aerolinea; }

    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }

    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }

    public LocalDateTime getFechaPartida() { return fechaPartida; }
    public void setFechaPartida(LocalDateTime fechaPartida) { this.fechaPartida = fechaPartida; }

    public LocalDateTime getFechaConsulta() { return fechaConsulta; }
    /* Ya no es necesario porque esta inicializada con LocalDateTime.now()
        public void setFechaConsulta(LocalDateTime fechaConsulta) {
               this.fechaConsulta = fechaConsulta;
        }
    */

    public Integer getDistancia() { return distancia; }
    public void setDistancia(Integer distancia) { this.distancia = distancia; }

    public String getPrevision() { return prevision; }
    public void setPrevision(String prevision) { this.prevision = prevision; }

    public double getProbabilidad() { return probabilidad; }
    public void setProbabilidad(double probabilidad) { this.probabilidad = probabilidad; }

}