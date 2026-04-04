package me.rentsignal.locationInfo.repository;

import me.rentsignal.locationInfo.entity.ProvinceIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProvinceIndexRepository extends JpaRepository<ProvinceIndex, Long> {
}
