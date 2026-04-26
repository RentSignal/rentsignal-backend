package me.rentsignal.locationInfo.repository;

import me.rentsignal.locationInfo.entity.NeighborhoodTransport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface NeighborhoodTransportRepository extends JpaRepository<NeighborhoodTransport, Long> {
}
