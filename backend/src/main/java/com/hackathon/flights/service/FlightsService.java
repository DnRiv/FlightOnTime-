package com.hackathon.flights.service;

import com.hackathon.flights.exception.ValidationException;
import com.hackathon.flights.repository.VuelosRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Service
public class FlightsService {

    private Set<String> aerolineasValidas;
    private Set<String> aeropuertosValidos;
    private Set<String> rutasValidas;

    private final VuelosRepository vuelosRepository;

    public FlightsService(VuelosRepository vuelosRepository) {
        this.vuelosRepository = vuelosRepository;
    }



    private Set<String> cargarSetDesdeCsv(String nombreArchivo) {
        Set<String> datos = new HashSet<>();

        try {
            ClassPathResource resource = new ClassPathResource(nombreArchivo);

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

                String linea;
                while ((linea = reader.readLine()) != null) {
                    linea = linea.trim(); // Limpiamos espacios en blanco al inicio y final

                    if (linea.isEmpty() || linea.startsWith("#")) {
                        continue;
                    }

                    datos.add(linea);
                }
            }
        } catch (Exception e) {
            throw new ValidationException(
                    "No se pudo cargar el archivo de configuración: " + nombreArchivo,
                    "ARCHIVO_PERDIDO",
                    e
            );
        }

        return datos;
    }

    @PostConstruct
    public void cargarDatosValidacion() {
        aerolineasValidas = cargarSetDesdeCsv("aerolineas.csv");
        aeropuertosValidos = cargarSetDesdeCsv("destino_valido.csv"); // origen y destino usan el mismo csv
        rutasValidas = cargarSetDesdeCsv("rutas_validas.csv"); // con separador
    }

    private void validarRuta(String aerolinea, String origen, String destino) {
        if (!aerolineasValidas.contains(aerolinea)) {
            throw new ValidationException("aerolinea '" + aerolinea + "' no soportada. "
                    + "Ejemplos: " + aerolineasValidas.stream().limit(3).toList());
        }

        if (!aeropuertosValidos.contains(origen)) {
            throw new ValidationException("origen '" + origen + "' no es un aeropuerto válido.");
        }

        if (!aeropuertosValidos.contains(destino)) {
            throw new ValidationException("destino '" + destino + "' no es un aeropuerto válido.");
        }

        if (!rutasValidas.contains(aerolinea + "|" + origen + "|" + destino)) {
            throw new ValidationException("Ruta no soportada: " + aerolinea + " " + origen + " → " + destino);
        }
    }
}