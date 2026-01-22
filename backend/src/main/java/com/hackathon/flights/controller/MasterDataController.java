package com.hackathon.flights.controller;

import com.hackathon.flights.entity.Aerolinea;
import com.hackathon.flights.entity.Aeropuerto;
import com.hackathon.flights.entity.RutaValida;
import com.hackathon.flights.repository.AerolineaRepository;
import com.hackathon.flights.repository.AeropuertoRepository;
import com.hackathon.flights.repository.RutaValidaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Tag(name = "Datos Maestros", description = "Endpoints para obtener listas de referencia")
public class MasterDataController {

    @Autowired
    private AerolineaRepository aerolineaRepository;

    @Autowired
    private AeropuertoRepository aeropuertoRepository;

    @Autowired
    private RutaValidaRepository rutaValidaRepository;

    @GetMapping("/aerolineas")
    @Operation(summary = "Obtener lista de aerolíneas soportadas")
    public List<String> obtenerAerolineas() {
        return aerolineaRepository.findAll()
                .stream()
                .map(Aerolinea::getCodigo)
                .sorted()
                .collect(Collectors.toList());
    }

    @GetMapping("/aeropuertos")
    @Operation(summary = "Obtener lista de aeropuertos soportados")
    public List<String> obtenerAeropuertos() {
        return aeropuertoRepository.findAll()
                .stream()
                .map(Aeropuerto::getCodigoIata)
                .sorted()
                .collect(Collectors.toList());
    }

    @GetMapping("/ruta/distancia")
    @Operation(summary = "Obtener distancia real entre origen y destino")
    @ApiResponse(responseCode = "200", description = "Distancia en millas (entero)")
    public ResponseEntity<Integer> obtenerDistancia(
            @RequestParam String aerolinea,
            @RequestParam String origen,
            @RequestParam String destino) {

        Optional<RutaValida> ruta = rutaValidaRepository
                .findByAerolineaAndOrigenAndDestino(aerolinea, origen, destino);

        if (ruta.isPresent()) {
            return ResponseEntity.ok(ruta.get().getDistancia());
        } else {
            return ResponseEntity.ok(1);
        }
    }
}

