package me.rentsignal.locationInfo.repository;

import me.rentsignal.locationInfo.entity.NeighborhoodConvenience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NeighborhoodConvenienceRepository extends JpaRepository<NeighborhoodConvenience, Long> {
}
