package com.hackathon.flights.repository;

import com.hackathon.flights.entity.Aeropuerto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AeropuertoRepository extends JpaRepository<Aeropuerto, Long> {
    boolean existsByCodigoIata(String codigoIata);
}

