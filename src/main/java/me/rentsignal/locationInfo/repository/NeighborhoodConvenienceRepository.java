package me.rentsignal.locationInfo.repository;

import me.rentsignal.locationInfo.dto.NeighborhoodConvenienceQueryDto;
import me.rentsignal.locationInfo.entity.ConvenienceType;
import me.rentsignal.locationInfo.entity.NeighborhoodConvenience;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NeighborhoodConvenienceRepository extends JpaRepository<NeighborhoodConvenience, Long> {
    @Query("""
        SELECT new me.rentsignal.locationInfo.dto.NeighborhoodConvenienceQueryDto(
            nc.neighborhood.id,
            CONCAT(
                nc.neighborhood.district.name, ' ', nc.neighborhood.name
            ),
            COUNT(nc)
        )
        FROM NeighborhoodConvenience nc
        GROUP BY nc.neighborhood.id, nc.neighborhood.name, nc.neighborhood.district.name
        ORDER BY COUNT(nc) DESC 
    """)
    List<NeighborhoodConvenienceQueryDto> findTopNeighborhoodConvenienceCount(Pageable pageable);
    List<NeighborhoodConvenience> findByNeighborhood_IdAndType(Long id, ConvenienceType type);
}
