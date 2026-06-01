package me.rentsignal.locationInfo.repository;

import me.rentsignal.locationInfo.entity.ProvinceIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProvinceIndexRepository extends JpaRepository<ProvinceIndex, Long> {
    Optional<ProvinceIndex> findByProvince_NameAndBaseYearMonth(String name, String baseYearMonth);
    List<ProvinceIndex> findByProvince_NameAndBaseYearMonthBetweenOrderByBaseYearMonthAsc(String name, String start, String end);
    @Query("""
    SELECT MAX(p.baseYearMonth)
    FROM ProvinceIndex p
""")
    String findLatestBaseYearMonth();
}
