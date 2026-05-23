package me.rentsignal.locationInfo.repository;

import me.rentsignal.locationInfo.entity.DistrictIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistrictIndexRepository extends JpaRepository<DistrictIndex, Long> {
    List<DistrictIndex> findByDistrict_Province_NameAndBaseYearMonthOrderBySubwayAccessibilityIndexDesc(String provinceName, String baseYearMonth);
}
