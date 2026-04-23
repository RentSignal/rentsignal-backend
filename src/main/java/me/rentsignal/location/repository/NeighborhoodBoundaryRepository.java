package me.rentsignal.location.repository;

import me.rentsignal.location.entity.NeighborhoodBoundary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NeighborhoodBoundaryRepository extends JpaRepository<NeighborhoodBoundary, Long> {
    @Query("SELECT nb.neighborhood.code FROM NeighborhoodBoundary nb")
    List<String> findAllNeighborhoodCodes();
}
