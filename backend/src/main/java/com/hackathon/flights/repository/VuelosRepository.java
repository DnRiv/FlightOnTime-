package com.hackathon.flights.repository;

import com.hackathon.flights.entity.Vuelos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VuelosRepository extends JpaRepository<Vuelos, Long> {
    // JpaRepository ya provee: save(), findById(), findAll(), delete(), etc.
}
