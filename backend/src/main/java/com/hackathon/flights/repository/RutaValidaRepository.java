package com.hackathon.flights.repository;

import com.hackathon.flights.entity.RutaValida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RutaValidaRepository extends JpaRepository<RutaValida, Long> {

    boolean existsByAerolineaAndOrigenAndDestino(String aerolinea, String origen, String destino);

    // Opcional: si en el futuro necesitas más info, pero para validación basta el boolean
}

