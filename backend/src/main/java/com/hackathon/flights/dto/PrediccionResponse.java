package com.hackathon.flights.dto;

public class PrediccionResponse {

    private final String prevision;
    private final double probabilidad;  // o float, según DS

    public PrediccionResponse(String prevision, double probabilidad) {
        this.prevision = prevision;
        this.probabilidad = probabilidad;
    }

    public String getPrevision() { return prevision; }
    public double getProbabilidad() { return probabilidad; }
}
