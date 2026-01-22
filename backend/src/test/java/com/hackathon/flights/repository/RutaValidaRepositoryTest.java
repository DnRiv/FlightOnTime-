package com.hackathon.flights.repository;

import com.hackathon.flights.entity.RutaValida;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RutaValidaRepositoryTest {

    @Autowired
    private RutaValidaRepository repository;

    @Test
    void findByAerolineaAndOrigenAndDestino_DeberiaDevolverRuta() {
        // Given
        RutaValida ruta = new RutaValida();
        ruta.setAerolinea("AA");
        ruta.setOrigen("JFK");
        ruta.setDestino("LAX");
        ruta.setDistancia(2500);
        repository.save(ruta);

        // When
        var resultado = repository.findByAerolineaAndOrigenAndDestino("AA", "JFK", "LAX");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getDistancia()).isEqualTo(2500);
    }
}

