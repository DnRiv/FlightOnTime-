package com.hackathon.flights.service;

import com.hackathon.flights.dto.DsRequest;
import com.hackathon.flights.dto.PrediccionLoteResponse;
import com.hackathon.flights.dto.PrediccionResponse;
import com.hackathon.flights.dto.VuelosRequest;
import com.hackathon.flights.entity.Vuelos;
import com.hackathon.flights.exception.ValidationException;
import com.hackathon.flights.repository.AerolineaRepository;
import com.hackathon.flights.repository.AeropuertoZonaRepository;
import com.hackathon.flights.repository.RutaValidaRepository;
import com.hackathon.flights.repository.VuelosRepository;
import com.hackathon.flights.repository.AeropuertoRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FlightsService {

    private final VuelosRepository vuelosRepository;
    private final RestTemplate restTemplate;

    private final AerolineaRepository aerolineaRepository;
    private final AeropuertoZonaRepository aeropuertoZonaRepository;
    private final RutaValidaRepository rutaValidaRepository;
    private final AeropuertoRepository aeropuertoRepository;

    public FlightsService(
            VuelosRepository vuelosRepository,
            RestTemplate restTemplate,
            AerolineaRepository aerolineaRepository,
            AeropuertoZonaRepository aeropuertoZonaRepository,
            RutaValidaRepository rutaValidaRepository,
            AeropuertoRepository aeropuertoRepository) { // ← añadido
        this.vuelosRepository = vuelosRepository;
        this.restTemplate = restTemplate;
        this.aerolineaRepository = aerolineaRepository;
        this.aeropuertoZonaRepository = aeropuertoZonaRepository;
        this.rutaValidaRepository = rutaValidaRepository;
        this.aeropuertoRepository = aeropuertoRepository; // ← añadido
    }

    public void validarVuelo(VuelosRequest request) {
        String aerolinea = request.getAerolinea();
        String origen = request.getOrigen();
        String destino = request.getDestino();
        LocalDateTime fechaPartida = request.getFechaPartida();

        // Validar aerolínea
        if (!aerolineaRepository.existsByCodigo(aerolinea)) {
            throw new ValidationException(
                    "aerolinea '" + aerolinea + "' no soportada.",
                    "AEROLINEA_INVALIDA"
            );
        }

        // Validar aeropuertos válidos (de destino_valido.csv)
        if (!aeropuertoRepository.existsByCodigoIata(origen)) {
            throw new ValidationException("origen '" + origen + "' no es un aeropuerto válido.", "AEROPUERTO_INVALIDO");
        }
        if (!aeropuertoRepository.existsByCodigoIata(destino)) {
            throw new ValidationException("destino '" + destino + "' no es un aeropuerto válido.", "AEROPUERTO_INVALIDO");
        }

        // Validar ruta
        if (!rutaValidaRepository.existsByAerolineaAndOrigenAndDestino(aerolinea, origen, destino)) {
            throw new ValidationException(
                    "Ruta no soportada: " + aerolinea + " " + origen + " → " + destino,
                    "RUTA_INVALIDA"
            );
        }

        // Validar hora futura
        validarHoraFutura(fechaPartida, origen);
    }

    private boolean existeZonaHoraria(String iata) {
        return aeropuertoZonaRepository.findByCodigoIata(iata).isPresent();
    }

    private String obtenerZona(String iata) {
        return aeropuertoZonaRepository.findByCodigoIata(iata)
                .map(az -> az.getZonaHoraria())
                .orElse("America/New_York");
    }

    private void validarHoraFutura(LocalDateTime horaLocal, String origen) {
        String zonaId = obtenerZona(origen);
        ZonedDateTime horaSalida = horaLocal.atZone(ZoneId.of(zonaId));
        ZonedDateTime ahoraUTC = ZonedDateTime.now(ZoneOffset.UTC);

        System.out.println("🔍 Validando: " +
                horaSalida + " → UTC: " + horaSalida.toInstant() +
                " | Ahora UTC: " + ahoraUTC.toInstant());

        if (horaSalida.isBefore(ahoraUTC)) {
            throw new ValidationException(
                    "La fecha de partida debe ser futura en " + origen,
                    "FECHA_PASADA"
            );
        }
    }

    // ───────────────────────────────────────────────
    // MÉTODOS EXISTENTES (sin cambios en lógica)
    // ───────────────────────────────────────────────

    @Transactional
    public PrediccionResponse predecir(VuelosRequest request) {
        validarVuelo(request);

        Vuelos vuelo = new Vuelos(
                request.getAerolinea(),
                request.getOrigen(),
                request.getDestino(),
                request.getFechaPartida(),
                request.getDistancia()
        );

        PrediccionResponse prediccion = llamarModeloDS(request);
        vuelo.setPrevision(prediccion.getPrevision());
        vuelo.setProbabilidad(prediccion.getProbabilidad());
        System.out.println("💾 Guardando vuelo: " + vuelo.getAerolinea() + " " + vuelo.getOrigen());
        vuelosRepository.save(vuelo);
        System.out.println("✅ Vuelo guardado con ID: " + vuelo.getId());

        return prediccion;
    }


    private PrediccionResponse llamarModeloDS(VuelosRequest request) {
        DsRequest dsRequest = new DsRequest(
                request.getAerolinea(),
                request.getOrigen(),
                request.getDestino(),
                request.getFechaPartida(),
                request.getDistancia()
        );

        try {
            ResponseEntity<java.util.Map<String, Object>> response = restTemplate.exchange(
                    "http://localhost:8000/predict",
                    org.springframework.http.HttpMethod.POST,
                    new HttpEntity<>(dsRequest),
                    new ParameterizedTypeReference<java.util.Map<String, Object>>() {}
            );

            java.util.Map<String, Object> body = response.getBody();
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

    @Transactional
    public List<PrediccionLoteResponse> predecirLote(MultipartFile file) {
        List<PrediccionLoteResponse> resultados = new ArrayList<>();
        int fila = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String cabecera = reader.readLine();
            fila++;
            if (cabecera == null) {
                throw new ValidationException("Archivo CSV vacío.", "CSV_VACIO");
            }
            // Validar cabecera (asumimos formato fijo)
            String[] headers = cabecera.trim().split(",");
            if (headers.length != 5) {
                throw new ValidationException(
                        "Formato de cabecera inválido. Se esperaba: aerolinea,origen,destino,fecha_partida,distancia",
                        "CSV_CABECERA_INVALIDA"
                );
            }

            String linea;
            while ((linea = reader.readLine()) != null) {
                fila++;
                linea = linea.trim();
                if (linea.isEmpty()) continue;

                try {
                    String[] campos = linea.split(",");
                    if (campos.length != 5) {
                        throw new ValidationException("Fila con " + campos.length + " columnas (se esperan 5)");
                    }

                    String aerolinea = campos[0].trim().toUpperCase();
                    String origen = campos[1].trim().toUpperCase();
                    String destino = campos[2].trim().toUpperCase();
                    String fechaPartidaStr = campos[3].trim();
                    String distanciaStr = campos[4].trim();

                    if (aerolinea.isEmpty() || origen.isEmpty() || destino.isEmpty() || fechaPartidaStr.isEmpty() || distanciaStr.isEmpty()) {
                        throw new ValidationException("Campos obligatorios ausentes en la fila");
                    }

                    LocalDateTime fechaPartida = LocalDateTime.parse(fechaPartidaStr);
                    Integer distancia = Integer.valueOf(distanciaStr);

                    VuelosRequest request = new VuelosRequest();
                    request.setAerolinea(aerolinea);
                    request.setOrigen(origen);
                    request.setDestino(destino);
                    request.setFechaPartida(fechaPartida);
                    request.setDistancia(distancia);

                    // 👇 Aquí usamos el nuevo método de validación
                    validarVuelo(request);

                    PrediccionResponse prediccion = llamarModeloDS(request);
                    Vuelos vuelo = new Vuelos(aerolinea, origen, destino, fechaPartida, distancia);
                    vuelo.setPrevision(prediccion.getPrevision());
                    vuelo.setProbabilidad(prediccion.getProbabilidad());
                    vuelosRepository.save(vuelo);

                    resultados.add(PrediccionLoteResponse.exito(
                            fila, aerolinea, origen, destino, fechaPartida, distancia,
                            prediccion.getPrevision(), prediccion.getProbabilidad()
                    ));

                } catch (Exception e) {
                    String mensaje = (e instanceof ValidationException)
                            ? e.getMessage()
                            : "Error al procesar fila: " + e.getMessage();

                    // Intentar extraer campos parciales
                    String aerolinea = "";
                    String origen = "";
                    String destino = "";
                    LocalDateTime fechaPartida = null;
                    Integer distancia = null;
                    try {
                        String[] campos = linea.split(",");
                        aerolinea = campos.length > 0 ? campos[0].trim() : "";
                        origen = campos.length > 1 ? campos[1].trim() : "";
                        destino = campos.length > 2 ? campos[2].trim() : "";
                        if (campos.length > 3) fechaPartida = LocalDateTime.parse(campos[3].trim());
                        if (campos.length > 4) distancia = Integer.valueOf(campos[4].trim());
                    } catch (Exception ignored) {}

                    resultados.add(PrediccionLoteResponse.error(
                            fila, aerolinea, origen, destino, fechaPartida, distancia,
                            "VALIDACION_FALLIDA", mensaje
                    ));
                }
            }
        } catch (ValidationException ve) {
            throw ve;
        } catch (Exception e) {
            throw new ValidationException(
                    "Error inesperado al leer el archivo CSV: " + e.getMessage(),
                    "CSV_ERROR_LECTURA", e
            );
        }

        if (resultados.isEmpty()) {
            throw new ValidationException("El archivo no contiene vuelos válidos para procesar.", "CSV_SIN_DATOS");
        }

        return resultados;
    }
}