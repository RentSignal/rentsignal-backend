package me.rentsignal.location.repository;

import me.rentsignal.location.entity.District;
import me.rentsignal.location.entity.Province;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DistrictRepository extends JpaRepository<District, Long> {
    Optional<District> findByNameAndProvince(String name, Province province);
}
