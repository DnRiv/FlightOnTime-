package com.hackathon.flights.service;

import com.hackathon.flights.dto.PrediccionResponse;
import com.hackathon.flights.dto.VuelosRequest;
import com.hackathon.flights.entity.AeropuertoZona;
import com.hackathon.flights.exception.ValidationException;
import com.hackathon.flights.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightsServiceTest {

    @Mock
    private VuelosRepository vuelosRepository;

    @Mock
    private AerolineaRepository aerolineaRepository;

    @Mock
    private AeropuertoRepository aeropuertoRepository;

    @Mock
    private RutaValidaRepository rutaValidaRepository;

    @Mock
    private AeropuertoZonaRepository aeropuertoZonaRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FlightsService flightsService;

    @BeforeEach
    void setUp() {
        // Usa lenient() para evitar UnnecessaryStubbingException
        lenient().when(aerolineaRepository.existsByCodigo(anyString())).thenReturn(false);
        lenient().when(aeropuertoRepository.existsByCodigoIata(anyString())).thenReturn(false);
        lenient().when(rutaValidaRepository.existsByAerolineaAndOrigenAndDestino(anyString(), anyString(), anyString())).thenReturn(false);
        lenient().when(aeropuertoZonaRepository.findByCodigoIata(anyString()))
                .thenReturn(Optional.of(new AeropuertoZona("DEFAULT", "America/New_York")));
    }

    private VuelosRequest crearRequestValido() {
        VuelosRequest request = new VuelosRequest();
        request.setAerolinea("AA");
        request.setOrigen("JFK");
        request.setDestino("LAX");
        request.setFechaPartida(LocalDateTime.now().plusDays(1));
        request.setDistancia(2500);
        return request;
    }

    @Test
    void predecir_DeberiaLanzarExcepcion_CuandoAerolineaInvalida() {
        VuelosRequest request = crearRequestValido();
        request.setAerolinea("XX");
        // No necesitamos stub adicional: por defecto, existsByCodigo devuelve false

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> flightsService.predecir(request)
        );

        assertTrue(exception.getMessage().contains("aerolinea 'XX' no soportada"));
    }

    @Test
    void predecir_DeberiaLanzarExcepcion_CuandoRutaInvalida() {
        VuelosRequest request = crearRequestValido();
        // Configuramos solo lo mínimo para que falle en la ruta
        lenient().when(aerolineaRepository.existsByCodigo("AA")).thenReturn(true);
        lenient().when(aeropuertoRepository.existsByCodigoIata("JFK")).thenReturn(true);
        lenient().when(aeropuertoRepository.existsByCodigoIata("XXX")).thenReturn(true);
        lenient().when(aeropuertoZonaRepository.findByCodigoIata("JFK"))
                .thenReturn(Optional.of(new AeropuertoZona("JFK", "America/New_York")));

        request.setDestino("XXX");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> flightsService.predecir(request)
        );

        assertTrue(exception.getMessage().contains("Ruta no soportada"));
    }

    @Test
    void predecir_DeberiaLanzarExcepcion_CuandoFechaPasada() {
        VuelosRequest request = crearRequestValido();
        lenient().when(aerolineaRepository.existsByCodigo("AA")).thenReturn(true);
        lenient().when(aeropuertoRepository.existsByCodigoIata("JFK")).thenReturn(true);
        lenient().when(aeropuertoRepository.existsByCodigoIata("LAX")).thenReturn(true);
        lenient().when(rutaValidaRepository.existsByAerolineaAndOrigenAndDestino("AA", "JFK", "LAX")).thenReturn(true);
        lenient().when(aeropuertoZonaRepository.findByCodigoIata("JFK"))
                .thenReturn(Optional.of(new AeropuertoZona("JFK", "America/New_York")));

        request.setFechaPartida(LocalDateTime.now().minusDays(1));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> flightsService.predecir(request)
        );

        assertTrue(exception.getMessage().contains("debe ser futura"));
    }
}