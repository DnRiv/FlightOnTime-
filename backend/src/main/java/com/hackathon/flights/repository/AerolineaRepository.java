package com.hackathon.flights.repository;

import com.hackathon.flights.entity.Aerolinea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AerolineaRepository extends JpaRepository<Aerolinea, Long> {
    Optional<Aerolinea> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
}

