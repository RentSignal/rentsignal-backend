package me.rentsignal.locationInfo.repository;

import me.rentsignal.locationInfo.entity.DistrictIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DistrictIndexRepository extends JpaRepository<DistrictIndex, Long> {
    List<DistrictIndex> findByDistrict_Province_NameAndBaseYearMonthOrderBySubwayAccessibilityIndexDesc(String provinceName, String baseYearMonth);
    Optional<DistrictIndex> findByDistrict_IdAndBaseYearMonth(Long districtId, String baseYearMonth);
    @Query("""
    SELECT MAX(d.baseYearMonth)
    FROM DistrictIndex d
""")
    String findLatestBaseYearMonth();
}
