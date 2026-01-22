package com.hackathon.flights.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class PrediccionResponse {

    @Schema(description = "Estado predicho: 'Puntual' o 'Retrasado'", example = "Retrasado")
    private final String prevision;

    @Schema(description = "Confianza en la predicción (0.0 a 1.0)", example = "0.71")
    private final double probabilidad;

    public PrediccionResponse(String prevision, double probabilidad) {
        this.prevision = prevision;
        this.probabilidad = probabilidad;
    }

    public String getPrevision() { return prevision; }
    public double getProbabilidad() { return probabilidad; }
}
