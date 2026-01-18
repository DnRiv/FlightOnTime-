package com.hackathon.flights.repository;

import com.hackathon.flights.entity.AeropuertoZona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AeropuertoZonaRepository extends JpaRepository<AeropuertoZona, Long> {
    Optional<AeropuertoZona> findByCodigoIata(String codigoIata);
}
