package me.rentsignal.locationInfo.repository;

import me.rentsignal.locationInfo.entity.NeighborhoodTransport;
import me.rentsignal.locationInfo.entity.TransportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface NeighborhoodTransportRepository extends JpaRepository<NeighborhoodTransport, Long> {
    List<NeighborhoodTransport> findByNeighborhood_District_IdAndTransportType(Long id, TransportType type);
    List<NeighborhoodTransport> findAllByTransportType(TransportType type);
}
