package me.rentsignal.locationInfo.repository;

import me.rentsignal.locationInfo.entity.DistrictSafety;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistrictSafetyRepository extends JpaRepository<DistrictSafety, Long> {
    List<DistrictSafety> findByDistrict_Province_NameOrderBySafetyScoreDesc(String provinceName);
}
