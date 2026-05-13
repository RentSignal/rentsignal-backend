package me.rentsignal.locationInfo.repository;

import me.rentsignal.locationInfo.entity.DistrictSafety;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DistrictSafetyRepository extends JpaRepository<DistrictSafety, Long> {
}
