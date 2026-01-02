package com.hackathon.flights.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.flights.dto.DsRequest;
import com.hackathon.flights.entity.Vuelos;
import com.hackathon.flights.exception.ValidationException;
import com.hackathon.flights.repository.VuelosRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.hackathon.flights.dto.PrediccionResponse;
import com.hackathon.flights.dto.VuelosRequest;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class FlightsService {

    private Set<String> aerolineasValidas;
    private Set<String> aeropuertosValidos;
    private Set<String> rutasValidas;

    private final VuelosRepository vuelosRepository;
    private final RestTemplate restTemplate;  // ← inyectado por constructor

    // Constructor con inyección de ambas dependencias
    public FlightsService(VuelosRepository vuelosRepository, RestTemplate restTemplate) {
        this.vuelosRepository = vuelosRepository;
        this.restTemplate = restTemplate;
    }

    private Set<String> cargarSetDesdeCsv(String nombreArchivo) {
        Set<String> datos = new HashSet<>();
        try {
            ClassPathResource resource = new ClassPathResource(nombreArchivo);
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String linea;
                while ((linea = reader.readLine()) != null) {
                    linea = linea.trim();
                    if (!linea.isEmpty() && !linea.startsWith("#")) {
                        datos.add(linea);
                    }
                }
            }
        } catch (Exception e) {
            throw new ValidationException(
                    "No se pudo cargar el archivo: " + nombreArchivo,
                    "ARCHIVO_PERDIDO",
                    e
            );
        }
        return datos;
    }

    @PostConstruct
    public void cargarDatosValidacion() {
        aerolineasValidas = cargarSetDesdeCsv("aerolineas.csv"); // Llama a archivos csv
        aeropuertosValidos = cargarSetDesdeCsv("destino_valido.csv");
        rutasValidas = cargarSetDesdeCsv("rutas_validas.csv");
        /* Para verificar que están cargando los archivos csv
        System.out.println("Aerolíneas cargadas: " + aerolineasValidas.size());
        System.out.println("Rutas cargadas: " + rutasValidas.size());
        System.out.println("Primera ruta: " + rutasValidas.iterator().next());
        */
    }

    private void validarRuta(String aerolinea, String origen, String destino) {
        /* Log para ver que valida - Borrar luego de encontrar error
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
        // Log para saber que se recibe el request
        // System.out.println("Recibido request CCZ: " + request.getAerolinea());

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

        // 3. Llamar a DS
        PrediccionResponse prediccion = llamarModeloDS(request);

        // 4. Completar entidad y guardar
        vuelo.setPrevision(prediccion.getPrevision());
        vuelo.setProbabilidad(prediccion.getProbabilidad());
        vuelosRepository.save(vuelo);

        return prediccion;
    }

    // Método privado, SIN @Autowired
    private PrediccionResponse llamarModeloDS(VuelosRequest request) {
        
        DsRequest dsRequest = new DsRequest(
                request.getAerolinea(),
                request.getOrigen(),
                request.getDestino(),
                request.getFechaPartida(),
                request.getDistancia()
        );

        // Borrar esta parte luego de encontrar el error
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(dsRequest);
            System.out.println("🚀 JSON enviado a DS: " + json);
        } catch (Exception e) {
            System.err.println("❌ Error al serializar DsRequest: " + e.getMessage());
        }

        try {

            // ✅ Usamos Map<String, Object> para mayor claridad
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    "http://localhost:8000/predict",
                    HttpMethod.POST,
                    new HttpEntity<>(dsRequest),
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> body = response.getBody();
            if (body == null || !body.containsKey("prediction") || !body.containsKey("probability")) {
                throw new ValidationException("Respuesta inválida de DS: " + body, "DS_ERROR");
            }

            String prediction = (String) body.get("prediction");
            Object probObj = body.get("probability");
            double probability = (probObj instanceof Number)
                    ? ((Number) probObj).doubleValue()
                    : Double.parseDouble(probObj.toString());

            String prevision = "on schedule".equals(prediction) ? "Puntual" : "Retrasado";
            return new PrediccionResponse(prevision, probability);

        } catch (ResourceAccessException e) {
            throw new ValidationException("DS no está disponible (¿corriendo en puerto 8000?)", "DS_OFFLINE", e);
        } catch (HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            throw new ValidationException("Error en DS: " + (errorBody.isEmpty() ? e.getMessage() : errorBody), "DS_ERROR", e);
        } catch (Exception e) {
            throw new ValidationException("Error inesperado al llamar a DS: " + e.getMessage(), "DS_ERROR", e);
        }
    }
}
