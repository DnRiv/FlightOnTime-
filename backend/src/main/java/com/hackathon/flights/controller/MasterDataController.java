package com.hackathon.flights.controller;

import com.hackathon.flights.entity.Aerolinea;
import com.hackathon.flights.entity.Aeropuerto;
import com.hackathon.flights.repository.AerolineaRepository;
import com.hackathon.flights.repository.AeropuertoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api") // ← Ahora sí: /api/aerolineas
public class MasterDataController {

    @Autowired
    private AerolineaRepository aerolineaRepository;

    @Autowired
    private AeropuertoRepository aeropuertoRepository;

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
}
