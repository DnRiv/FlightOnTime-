package com.hackathon.flights.controller;

import com.hackathon.flights.entity.Aerolinea;
import com.hackathon.flights.entity.Aeropuerto;
import com.hackathon.flights.entity.RutaValida;
import com.hackathon.flights.repository.AerolineaRepository;
import com.hackathon.flights.repository.AeropuertoRepository;
import com.hackathon.flights.repository.RutaValidaRepository;
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
public class MasterDataController {

    @Autowired
    private AerolineaRepository aerolineaRepository;

    @Autowired
    private AeropuertoRepository aeropuertoRepository;

    @Autowired
    private RutaValidaRepository rutaValidaRepository;

    @GetMapping("/aerolineas")
    public List<String> obtenerAerolineas() {
        return aerolineaRepository.findAll()
                .stream()
                .map(Aerolinea::getCodigo)
                .sorted()
                .collect(Collectors.toList());
    }

    @GetMapping("/aeropuertos")
    public List<String> obtenerAeropuertos() {
        return aeropuertoRepository.findAll()
                .stream()
                .map(Aeropuerto::getCodigoIata)
                .sorted()
                .collect(Collectors.toList());
    }

    @GetMapping("/ruta/distancia")
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

