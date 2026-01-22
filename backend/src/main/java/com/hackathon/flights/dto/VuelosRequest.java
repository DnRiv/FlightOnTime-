package com.hackathon.flights.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public class VuelosRequest {

    @Schema(description = "Código IATA de la aerolínea (2 caracteres)", example = "AA")
    @NotBlank(message = "aerolínea es obligatorio")
    @Pattern(
            regexp = "([A-Z]{2}|[A-Z][0-9]|[0-9][A-Z])",
            message = "aerolinea debe ser código IATA de 2 caracteres (ej: LA, 9E, B6)"
    )
    private String aerolinea;

    @Schema(description = "Código IATA del aeropuerto de origen (3 caracteres)", example = "JFK")
    @NotBlank(message = "origen es obligatorio")
    @Pattern(regexp = "[A-Z]{3}", message = "Origen debe ser codigo IATA de 3 letras (ej: SCL)")
    private String origen;

    @Schema(description = "Código IATA del aeropuerto de destino (3 caracteres)", example = "LAX")
    @NotBlank(message = "destino es obligatorio")
    @Pattern(regexp = "[A-Z]{3}", message = "Destino debe ser codigo IATA de 3 letras (ej: LIM)")
    private String destino;

    @Schema(description = "Fecha y hora de partida en formato ISO-8601", example = "2026-06-15T10:30")
    @NotNull(message = "La fecha de salida es obligatoria")
    @JsonProperty("fecha_partida")
    private LocalDateTime fechaPartida;

    @Schema(description = "Distancia en millas", example = "2500")
    @NotNull(message = "La Distancia es obligatoria")
    @Min(value = 1, message = "distancia debe ser mayor a 0")
    private Integer distancia;

    public VuelosRequest() {}

    // Getters y setters
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

