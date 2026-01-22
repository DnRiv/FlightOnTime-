package com.hackathon.flights.controller;

import com.hackathon.flights.dto.PrediccionLoteResponse;
import com.hackathon.flights.dto.PrediccionResponse;
import com.hackathon.flights.dto.VuelosRequest;
import com.hackathon.flights.service.FlightsService;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/predict")
@Tag(name = "Predicción de Vuelos", description = "Endpoints para predecir puntualidad de vuelos")
public class VueloController {

    private final FlightsService flightsService;

    public VueloController(FlightsService flightsService) {
        this.flightsService = flightsService;
    }

    @PostMapping
    @Operation(
            summary = "Predecir estado de un vuelo individual",
            description = "Valida los datos del vuelo contra la base de datos, consulta el modelo de Machine Learning y devuelve la predicción."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Predicción exitosa",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PrediccionResponse.class))
    )
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    public PrediccionResponse predecirVuelo(@Valid @RequestBody VuelosRequest request) {
        return flightsService.predecir(request);
    }

    @PostMapping(value = "/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Predecir estado de múltiples vuelos (lote)",
            description = "Procesa un archivo CSV con múltiples vuelos y devuelve un informe detallado de cada predicción."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Procesamiento exitoso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PrediccionLoteResponse.class))
    )
    @ApiResponse(responseCode = "400", description = "Archivo CSV inválido o vacío")
    public ResponseEntity<List<PrediccionLoteResponse>> predecirLote(
            @RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new ValidationException("El archivo CSV es obligatorio.");
        }
        if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            throw new ValidationException("Solo se aceptan archivos con extensión .csv");
        }
        if (file.getSize() == 0) {
            throw new ValidationException("El archivo CSV está vacío.");
        }

        List<PrediccionLoteResponse> resultados = flightsService.predecirLote(file);
        return ResponseEntity.ok(resultados);
    }
}

