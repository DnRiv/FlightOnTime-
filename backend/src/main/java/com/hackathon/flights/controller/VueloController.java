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

import java.util.List;


@RestController
@RequestMapping("/predict")
public class VueloController {

    private final FlightsService flightsService;

    public VueloController(FlightsService flightsService) {
        this.flightsService = flightsService;
    }

    @PostMapping
    public PrediccionResponse predecirVuelo(@Valid @RequestBody VuelosRequest request) {
        return flightsService.predecir(request);
    }

    @PostMapping(value = "/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<PrediccionLoteResponse>> predecirLote(
            @RequestParam("file") MultipartFile file) {

        // Validación básica del archivo (antes de delegar al servicio)
        if (file == null || file.isEmpty()) {
            throw new ValidationException("El archivo CSV es obligatorio.");
        }

        if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            throw new ValidationException("Solo se aceptan archivos con extensión .csv");
        }

        if (file.getSize() == 0) {
            throw new ValidationException("El archivo CSV está vacío.");
        }

        // Delegamos el procesamiento real al servicio
        List<PrediccionLoteResponse> resultados = flightsService.predecirLote(file);

        return ResponseEntity.ok(resultados);
    }
}

