package com.hackathon.flights.dto;

public class PrediccionResponse {

    private final String prevision;
    private final double probabilidad; // double más precision y es el estandar

    public PrediccionResponse(String prevision, double probabilidad) {
        this.prevision = prevision;
        this.probabilidad = probabilidad;
    }

    public String getPrevision() { return prevision; }
    public double getProbabilidad() { return probabilidad; }
}
