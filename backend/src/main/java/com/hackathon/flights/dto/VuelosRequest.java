package com.hackathon.flights.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public class VuelosRequest {

    @NotBlank(message = "aerolínea es obligatorio")
    @Pattern(
            regexp = "([A-Z]{2}|[A-Z][0-9]|[0-9][A-Z])",
            message = "aerolinea debe ser código IATA de 2 caracteres (ej: LA, 9E, B6)"
    )
    // @Size(min = 2, max = 2, message = "El texto debe tener dos caracteres")
    // Un forma diferente de realizar la validación
    private String aerolinea;

    @NotBlank(message = "origen es obligatorio")
    @Pattern(regexp = "[A-Z]{3}", message = "Origen debe ser codigo IATA de 3 letras (ej: SCL)")
    private String origen;

    @NotBlank(message = "destino es obligatorio")
    @Pattern(regexp = "[A-Z]{3}", message = "Destino debe ser codigo IATA de 3 letras (ej: LIM)")
    private String destino;

    @NotNull(message = "La fecha de salida es obligatoria")
    @FutureOrPresent(message = "La fecha debe ser actual o posterior")
    @JsonProperty("fecha_partida")
    private LocalDateTime fechaPartida;

    @NotNull(message = "La Distancia es obligatoria")
    @Min(value = 1, message = "distancia debe ser mayor a 0")
    // @JsonProperty("distancia_km") // En el desafío se entrega así
    private Integer distancia; // Dataset usa datos enteros

    // Constructor vacío
    public VuelosRequest() {}

    // Getter y Setter
    public String getAerolinea() { return aerolinea; }
    public void setAerolinea(String aerolinea) {
        this.aerolinea = aerolinea != null ? aerolinea.trim().toUpperCase() : null;
    }

    public String getOrigen() { return origen; }
    public void setOrigen(String origen) {
        this.origen = origen != null ? origen.trim().toUpperCase() : null;
    }

    public String getDestino() { return destino; }
    public void setDestino(String destino) {
        this.destino = destino != null ? destino.trim().toUpperCase() : null;
    }

    public LocalDateTime getFechaPartida() { return fechaPartida; }
    public void setFechaPartida(LocalDateTime fechaPartida) { this.fechaPartida = fechaPartida; }

    public Integer getDistancia() { return distancia; }
    public void setDistancia(Integer distancia) { this.distancia = distancia; }

}
