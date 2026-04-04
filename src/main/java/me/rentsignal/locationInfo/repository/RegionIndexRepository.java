package me.rentsignal.locationInfo.repository;

import me.rentsignal.locationInfo.entity.RegionIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionIndexRepository extends JpaRepository<RegionIndex, Long> {
}
