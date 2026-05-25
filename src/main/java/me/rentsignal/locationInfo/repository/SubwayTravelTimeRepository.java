package me.rentsignal.locationInfo.repository;

import me.rentsignal.locationInfo.entity.SubwayTravelTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubwayTravelTimeRepository extends JpaRepository<SubwayTravelTime, Long> {
}
