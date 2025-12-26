package com.hackathon.flights.service;

import com.hackathon.flights.entity.Vuelos;
import com.hackathon.flights.exception.ValidationException;
import com.hackathon.flights.repository.VuelosRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import com.hackathon.flights.dto.PrediccionResponse;
import com.hackathon.flights.dto.VuelosRequest;

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
        /* Para verificar que están cargando los archivos csv
        System.out.println("Aerolíneas cargadas: " + aerolineasValidas.size());
        System.out.println("Rutas cargadas: " + rutasValidas.size());
        System.out.println("Primera ruta: " + rutasValidas.iterator().next());
        */
    }

    private void validarRuta(String aerolinea, String origen, String destino) {
        /* Log para ver que valida
        System.out.println("Validando ruta: " + aerolinea + "|" + origen + "|" + destino);
        System.out.println("Rutas cargadas: " + rutasValidas.size());
         */
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

    public PrediccionResponse predecir(VuelosRequest request) {
        /* Para saber que se recibe el request
        System.out.println("Recibido request: " + request.getAerolinea());
        */
        // 1. Validar ruta
        validarRuta(request.getAerolinea(), request.getOrigen(), request.getDestino());

        // 2. Convertir a entidad
        Vuelos vuelo = new Vuelos(
                request.getAerolinea(),
                request.getOrigen(),
                request.getDestino(),
                request.getFechaPartida(),
                request.getDistancia()
        );

        // 3. Llamar a DS (mock por ahora)
        PrediccionResponse prediccion = mockLlamadaDS(request);

        // 4. Completar entidad y guardar
        vuelo.setPrevision(prediccion.getPrevision());
        vuelo.setProbabilidad(prediccion.getProbabilidad());
        vuelosRepository.save(vuelo);

        return prediccion;
    }

    private PrediccionResponse mockLlamadaDS(VuelosRequest request) {
        // Simula una predicción realista
        double probabilidad;
        if (request.getOrigen().equals("SCL") && request.getDestino().equals("LIM")) {
            probabilidad = 0.35; // Ruta puntual
        } else if (request.getFechaPartida().getHour() >= 22) {
            probabilidad = 0.80; // Vuelos nocturnos → más retrasos
        } else {
            probabilidad = Math.random(); // Aleatorio
        }
        String prevision = probabilidad > 0.5 ? "Retrasado" : "Puntual";
        return new PrediccionResponse(prevision, probabilidad);
    }
}
