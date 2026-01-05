package com.hackathon.flights.dto;

import java.time.LocalDateTime;
import com.hackathon.flights.controller.VueloController;

public class PrediccionLoteResponse {

    private final int fila;
    private final String aerolinea;
    private final String origen;
    private final String destino;
    private final LocalDateTime fechaPartida;
    private final Integer distancia;
    private final String estado;          // OK, VALIDACION_FALLIDA, DS_ERROR
    private final String mensajeError;    // null si OK
    private final String prevision;       // solo si OK
    private final Double probabilidad;    // solo si OK

    // Constructor completo
    public PrediccionLoteResponse(
            int fila,
            String aerolinea,
            String origen,
            String destino,
            LocalDateTime fechaPartida,
            Integer distancia,
            String estado,
            String mensajeError,
            String prevision,
            Double probabilidad) {
        this.fila = fila;
        this.aerolinea = aerolinea;
        this.origen = origen;
        this.destino = destino;
        this.fechaPartida = fechaPartida;
        this.distancia = distancia;
        this.estado = estado;
        this.mensajeError = mensajeError;
        this.prevision = prevision;
        this.probabilidad = probabilidad;
    }

    // Constructor auxiliar para errores (evita pasar nulls innecesarios)
    public static PrediccionLoteResponse error(
            int fila,
            String aerolinea,
            String origen,
            String destino,
            LocalDateTime fechaPartida,
            Integer distancia,
            String estado,
            String mensajeError) {
        return new PrediccionLoteResponse(
                fila, aerolinea, origen, destino, fechaPartida, distancia,
                estado, mensajeError,
                null, null
        );
    }

    // Constructor auxiliar para éxitos
    public static PrediccionLoteResponse exito(
            int fila,
            String aerolinea,
            String origen,
            String destino,
            LocalDateTime fechaPartida,
            Integer distancia,
            String prevision,
            Double probabilidad) {
        return new PrediccionLoteResponse(
                fila, aerolinea, origen, destino, fechaPartida, distancia,
                "OK", null,
                prevision, probabilidad
        );
    }

    // Getters
    public int getFila() { return fila; }
    public String getAerolinea() { return aerolinea; }
    public String getOrigen() { return origen; }
    public String getDestino() { return destino; }
    public LocalDateTime getFechaPartida() { return fechaPartida; }
    public Integer getDistancia() { return distancia; }
    public String getEstado() { return estado; }
    public String getMensajeError() { return mensajeError; }
    public String getPrevision() { return prevision; }
    public Double getProbabilidad() { return probabilidad; }
}

