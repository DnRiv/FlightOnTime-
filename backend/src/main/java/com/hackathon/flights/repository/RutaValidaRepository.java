// src/main/java/com/hackathon/flights/repository/RutaValidaRepository.java
package com.hackathon.flights.repository;

import com.hackathon.flights.entity.RutaValida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RutaValidaRepository extends JpaRepository<RutaValida, Long> {

    boolean existsByAerolineaAndOrigenAndDestino(String aerolinea, String origen, String destino);

    Optional<RutaValida> findByAerolineaAndOrigenAndDestino(String aerolinea, String origen, String destino);
}

