package com.hackathon.flights.controller;

import com.hackathon.flights.dto.PrediccionResponse;
import com.hackathon.flights.dto.VuelosRequest;
import com.hackathon.flights.service.FlightsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;


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
}
