package me.rentsignal.location.repository;

import me.rentsignal.location.entity.Neighborhood;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NeighborhoodRepository extends JpaRepository<Neighborhood, Long> {

    List<Neighborhood> findTop10ByNameContaining(String keyword);
}