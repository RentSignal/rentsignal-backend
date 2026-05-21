package me.rentsignal.locationInfo.repository;

import me.rentsignal.locationInfo.entity.HousingType;
import me.rentsignal.locationInfo.entity.RegionIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegionIndexRepository extends JpaRepository<RegionIndex, Long> {
    List<RegionIndex> findByHousingTypeAndBaseYearMonthOrderByRentCompositeIndexDesc(HousingType housingType, String baseYearMonth);
    List<RegionIndex> findByHousingTypeAndBaseYearMonth(HousingType housingType, String baseYearMonth);
}
