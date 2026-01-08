package com.hackathon.flights.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class DsRequest {
    @JsonProperty("airline")
    private String airline;

    @JsonProperty("origin")
    private String origin;

    @JsonProperty("destination")
    private String destination;

    @JsonProperty("departure_time")
    private String departure_time;

    @JsonProperty("distance_miles")
    private Integer distance_miles;

    // constructor, getters
    public DsRequest(String airline, String origin, String destination,
                     LocalDateTime departureTime, Integer distancia) {
        this.airline = airline;
        this.origin = origin;
        this.destination = destination;
        this.departure_time = departureTime.toString(); // DS espera ISO sin zona
        this.distance_miles = distancia;
    }

    public String getAirline() { return airline; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public String getDeparture_time() { return departure_time; }
    public Integer getDistance_miles() { return distance_miles; }
}
