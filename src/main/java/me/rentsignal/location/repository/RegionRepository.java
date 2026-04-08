package me.rentsignal.location.repository;

import me.rentsignal.location.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
    Optional<Region> findByAreaGroupAndAreaName(String areaGroup, String areaName);
}
