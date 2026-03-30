package me.rentsignal.location.repository;

import me.rentsignal.location.entity.Neighborhood;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NeighborhoodRepository extends JpaRepository<Neighborhood, Long> {
}