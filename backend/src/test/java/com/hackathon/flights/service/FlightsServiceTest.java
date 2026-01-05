package com.hackathon.flights.service;

import com.hackathon.flights.dto.PrediccionResponse;
import com.hackathon.flights.dto.VuelosRequest;
import com.hackathon.flights.entity.Vuelos;
import com.hackathon.flights.exception.ValidationException;
import com.hackathon.flights.repository.VuelosRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// 1. @ExtendWith: Le dice a JUnit que use Mockito para inicializar los @Mock
@ExtendWith(MockitoExtension.class)
class FlightsServiceTest {

    // 2. @Mock: Crea objetos "falsos" de las dependencias
    @Mock
    private VuelosRepository vuelosRepository;

    @Mock
    private RestTemplate restTemplate;

    // 3. @InjectMocks: Crea el servicio real e inyecta los Mocks anteriores dentro de él
    @InjectMocks
    private FlightsService flightsService;

    @BeforeEach
    void setUp() {
        // TRUCO IMPORTANTE:
        // Como FlightsService carga datos de CSV en @PostConstruct, y aquí no queremos
        // leer archivos reales (eso sería un test de integración), inyectamos manualmente
        // los Sets de validación usando ReflectionTestUtils.

        Set<String> aerolineas = new HashSet<>(Set.of("AA", "DL", "9C"));
        Set<String> aeropuertos = new HashSet<>(Set.of("MIA", "JFK", "TIP"));
        Set<String> rutas = new HashSet<>(Set.of("AA|MIA|JFK", "9C|JFK|TTP"));

        // ✅ Cargar mapaZonas para tests
        Map<String, String> mapaZonas = Map.of(
                "JFK", "America/New_York",
                "LAX", "America/Los_Angeles",
                "DEN", "America/Denver",
                "PHX", "America/Phoenix",
                "ORD", "America/Chicago",
                "DFW", "America/Chicago",
                "ATL", "America/New_York",
                "TTP", "America/Los_Angeles",
                "MIA", "America/New_York",
                "TIP", "America/Denver"
                // Añade más si tus tests los usan
        );

        // Inyectamos estos datos "falsos" en las variables privadas del servicio
        ReflectionTestUtils.setField(flightsService, "aerolineasValidas", aerolineas);
        ReflectionTestUtils.setField(flightsService, "aeropuertosValidos", aeropuertos);
        ReflectionTestUtils.setField(flightsService, "rutasValidas", rutas);
        ReflectionTestUtils.setField(flightsService, "mapaZonas", mapaZonas);

    }

    @Test
    void predecir_DeberiaRetornarPuntual_CuandoApiRespondeOnSchedule() {
        // --- A. ARRANGE (Preparar datos) ---

        // 1. Crear el Request de entrada (Datos válidos según nuestro setUp)
        VuelosRequest request = new VuelosRequest();
        request.setAerolinea("AA");
        request.setOrigen("MIA");
        request.setDestino("JFK");
        // Importante para que el Test funcione debe ser una fecha futura
        // request.setFechaPartida(LocalDateTime.of(2026, 12, 30, 10, 0));
        request.setFechaPartida(LocalDateTime.now().plusDays(1));
        request.setDistancia(1000);

        // 2. Simular la respuesta que daría Python (JSON simulado)
        Map<String, Object> respuestaPython = new HashMap<>();
        respuestaPython.put("prediction", "on schedule"); // Python dice "on schedule"
        respuestaPython.put("probability", 0.95);

        // Envolvemos eso en un ResponseEntity, que es lo que devuelve RestTemplate
        ResponseEntity<Map<String, Object>> responseEntity =
                new ResponseEntity<>(respuestaPython, HttpStatus.OK);

        // 3. ENSEÑAR AL MOCK (Stubbing):
        // "Cuando llames a exchange(...) con cualquier parámetro, devuelve 'responseEntity'"
        when(restTemplate.exchange(
                eq("http://localhost:8000/predict"), // URL exacta
                eq(HttpMethod.POST),                // Método exacto
                any(HttpEntity.class),              // Cualquier cuerpo (para no complicar el test)
                any(ParameterizedTypeReference.class) // Cualquier tipo de respuesta esperado
        )).thenReturn(responseEntity);

        // --- B. ACT (Ejecutar la lógica) ---
        PrediccionResponse resultado = flightsService.predecir(request);

        // --- C. ASSERT (Verificar resultados) ---

        // Verificamos que nuestro Java tradujo "on schedule" a "Puntual" [cite: 93]
        assertEquals("Puntual", resultado.getPrevision());
        assertEquals(0.95, resultado.getProbabilidad());

        // Verificamos que el servicio intentó guardar en la base de datos [cite: 81]
        verify(vuelosRepository).save(any(Vuelos.class));
    }

    @Test
    void predecir_DeberiaLanzarExcepcion_CuandoRutaEsInvalida() {
        // Arrange: datos completos, pero con aerolínea NO válida
        VuelosRequest request = new VuelosRequest();
        request.setAerolinea("XX"); // Importante: aerolínea NO debe estar en los datos de prueba
        request.setOrigen("MIA");
        request.setDestino("JFK");
        request.setFechaPartida(LocalDateTime.now().plusHours(2));
        request.setDistancia(1500);

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> flightsService.predecir(request)
        );

        // Assert: mensaje debe incluir "XX", no "AA"
        assertTrue(exception.getMessage().contains("aerolinea 'XX' no soportada"));
        // ✅ No se llama a DS → no hay error de response null
    }


}
