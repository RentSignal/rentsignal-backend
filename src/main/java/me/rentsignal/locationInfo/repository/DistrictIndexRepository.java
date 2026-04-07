package me.rentsignal.locationInfo.repository;

import me.rentsignal.locationInfo.entity.DistrictIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DistrictIndexRepository extends JpaRepository<DistrictIndex, Long> {
}
