package com.hackathon.flights.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.flights.dto.DsRequest;
import com.hackathon.flights.dto.PrediccionLoteResponse;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

@Service
public class FlightsService {

    private Set<String> aerolineasValidas;
    private Set<String> aeropuertosValidos;
    private Set<String> rutasValidas;
    private Map<String, String> mapaZonas;

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
        mapaZonas = cargarMapaDesdeCsv("aeropuertos_zonas_usa.csv");
        /* Para verificar que están cargando los archivos csv
        System.out.println("Aerolíneas cargadas: " + aerolineasValidas.size());
        System.out.println("Rutas cargadas: " + rutasValidas.size());
        System.out.println("Primera ruta: " + rutasValidas.iterator().next());
        */
        /* Logs para ver que cargue bien aeropuertos_zonas_usa.csv
        System.out.println("Zonas cargadas: " + mapaZonas.size());
        System.out.println("JFK → " + mapaZonas.get("JFK"));
        */
    }

    private Map<String, String> cargarMapaDesdeCsv(String nombreArchivo) {
        Map<String, String> mapa = new HashMap<>();
        try {
            ClassPathResource resource = new ClassPathResource(nombreArchivo);
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String linea;
                while ((linea = reader.readLine()) != null) {
                    linea = linea.trim();
                    // Saltar líneas vacías y comentarios
                    if (linea.isEmpty() || linea.startsWith("#")) continue;

                    // Dividir por coma (asumiendo formato: IATA,ZONA)
                    String[] partes = linea.split(",", 2); // máximo 2 partes
                    if (partes.length == 2) {
                        String clave = partes[0].trim();
                        String valor = partes[1].trim();
                        mapa.put(clave, valor);
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
        return mapa;
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

        // ✅ Nueva validación: hora futura en zona del origen
        validarHoraFutura(request.getFechaPartida(), request.getOrigen());

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

    private void validarHoraFutura(LocalDateTime horaLocal, String origen) {
        // 1. Obtener zona del aeropuerto de origen
        String zonaId = obtenerZona(origen);

        // 2. Interpretar la hora como local al origen
        ZonedDateTime horaSalida = horaLocal.atZone(ZoneId.of(zonaId));

        // 3. Obtener el instante actual en UTC (para comparación justa)
        ZonedDateTime ahoraUTC = ZonedDateTime.now(ZoneOffset.UTC);

        // 🔍 Diagnóstico (opcional, para desarrollo)
        System.out.println("🔍 Validando: " +
                horaSalida + " → UTC: " + horaSalida.toInstant() +
                " | Ahora UTC: " + ahoraUTC.toInstant());

        // 4. Comparar: ¿la hora de salida es anterior al presente en UTC?
        if (horaSalida.isBefore(ahoraUTC)) {
            throw new ValidationException(
                    "La fecha de partida debe ser futura en " + origen,
                    "FECHA_PASADA"
            );
        }
    }

    private String obtenerZona(String iata) {
        // ✅ Usamos el mapa que ya cargaremos (como aerolineasValidas)
        return mapaZonas.getOrDefault(iata, "America/New_York"); // fallback razonable para EE.UU.
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

    public List<PrediccionLoteResponse> predecirLote(MultipartFile file) {
        List<PrediccionLoteResponse> resultados = new ArrayList<>();
        int fila = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String cabecera = reader.readLine();
            fila++;

            // Validar cabecera
            if (cabecera == null) {
                throw new ValidationException("Archivo CSV vacío.", "CSV_VACIO");
            }

            String[] headers = cabecera.trim().split(",");
            if (headers.length != 5 ||
                    !"aerolinea".equalsIgnoreCase(headers[0]) ||
                    !"origen".equalsIgnoreCase(headers[1]) ||
                    !"destino".equalsIgnoreCase(headers[2]) ||
                    !"fecha_partida".equalsIgnoreCase(headers[3]) ||
                    !"distancia".equalsIgnoreCase(headers[4])) {
                throw new ValidationException(
                        "Formato de cabecera inválido. Se esperaba: aerolinea,origen,destino,fecha_partida,distancia",
                        "CSV_CABECERA_INVALIDA"
                );
            }

            // Procesar cada fila
            String linea;
            while ((linea = reader.readLine()) != null) {
                fila++;
                linea = linea.trim();
                if (linea.isEmpty()) continue; // saltar líneas vacías

                try {
                    String[] campos = linea.split(",");
                    if (campos.length != 5) {
                        throw new ValidationException("Fila con " + campos.length + " columnas (se esperan 5)");
                    }

                    // Parsear campos y convertir a mayúsculas
                    String aerolinea = campos[0].trim().toUpperCase();
                    String origen = campos[1].trim().toUpperCase();
                    String destino = campos[2].trim().toUpperCase();
                    String fechaPartidaStr = campos[3].trim();
                    String distanciaStr = campos[4].trim();

                    // Validar no vacíos
                    if (aerolinea.isEmpty() || origen.isEmpty() || destino.isEmpty() ||
                            fechaPartidaStr.isEmpty() || distanciaStr.isEmpty()) {
                        throw new ValidationException("Campos obligatorios ausentes en la fila");
                    }

                    // Parsear fecha y distancia
                    LocalDateTime fechaPartida = LocalDateTime.parse(fechaPartidaStr);
                    Integer distancia = Integer.valueOf(distanciaStr);

                    // Crear request y validar como en flujo individual
                    VuelosRequest request = new VuelosRequest();
                    request.setAerolinea(aerolinea);
                    request.setOrigen(origen);
                    request.setDestino(destino);
                    request.setFechaPartida(fechaPartida);
                    request.setDistancia(distancia);

                    // Validaciones reutilizadas
                    validarRuta(aerolinea, origen, destino);
                    validarHoraFutura(fechaPartida, origen);

                    // Llamar a DS y guardar (como en predecir())
                    PrediccionResponse prediccion = llamarModeloDS(request);

                    Vuelos vuelo = new Vuelos(aerolinea, origen, destino, fechaPartida, distancia);
                    vuelo.setPrevision(prediccion.getPrevision());
                    vuelo.setProbabilidad(prediccion.getProbabilidad());
                    vuelosRepository.save(vuelo);

                    // ✅ Éxito
                    resultados.add(PrediccionLoteResponse.exito(
                            fila, aerolinea, origen, destino, fechaPartida, distancia,
                            prediccion.getPrevision(), prediccion.getProbabilidad()
                    ));

                } catch (Exception e) {
                    // ❌ Error en esta fila → capturamos y seguimos
                    String mensaje = (e instanceof ValidationException)
                            ? e.getMessage()
                            : "Error al procesar fila: " + e.getMessage();

                    // Para evitar nulls en campos parseados
                    String aerolinea = "";
                    String origen = "";
                    String destino = "";
                    LocalDateTime fechaPartida = null;
                    Integer distancia = null;

                    // Intentar extraer lo que sí se pudo
                    try {
                        String[] campos = linea.split(",");
                        aerolinea = campos.length > 0 ? campos[0].trim() : "";
                        origen = campos.length > 1 ? campos[1].trim() : "";
                        destino = campos.length > 2 ? campos[2].trim() : "";
                        if (campos.length > 3) {
                            fechaPartida = LocalDateTime.parse(campos[3].trim());
                        }
                        if (campos.length > 4) {
                            distancia = Integer.valueOf(campos[4].trim());
                        }
                    } catch (Exception ignored) {
                        // Si falla, dejamos valores vacíos
                    }

                    resultados.add(PrediccionLoteResponse.error(
                            fila, aerolinea, origen, destino, fechaPartida, distancia,
                            "VALIDACION_FALLIDA", mensaje
                    ));
                }
            }

        } catch (ValidationException ve) {
            // Error estructural del CSV (no por fila)
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

