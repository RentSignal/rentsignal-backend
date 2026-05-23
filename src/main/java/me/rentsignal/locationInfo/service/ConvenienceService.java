package me.rentsignal.locationInfo.service;

import lombok.RequiredArgsConstructor;
import me.rentsignal.global.exception.BaseException;
import me.rentsignal.global.exception.ErrorCode;
import me.rentsignal.location.entity.Neighborhood;
import me.rentsignal.location.repository.NeighborhoodRepository;
import me.rentsignal.locationInfo.dto.ConvenienceRankDto;
import me.rentsignal.locationInfo.dto.ConvenienceTypeCountDto;
import me.rentsignal.locationInfo.dto.NeighborhoodConvenienceQueryDto;
import me.rentsignal.locationInfo.entity.ConvenienceType;
import me.rentsignal.locationInfo.repository.NeighborhoodConvenienceRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConvenienceService {

    private final NeighborhoodConvenienceRepository neighborhoodConvenienceRepository;
    private final NeighborhoodRepository neighborhoodRepository;

    public ConvenienceRankDto getConvenienceRanking() {
        List<NeighborhoodConvenienceQueryDto> topNeighborhoodConvenienceCount = neighborhoodConvenienceRepository.findTopNeighborhoodConvenienceCount(PageRequest.of(0, 7));

        List<ConvenienceRankDto.NeighborhoodConvenienceRankDto> ranking = new ArrayList<>();
        int i = 1;
        for (NeighborhoodConvenienceQueryDto dto : topNeighborhoodConvenienceCount) {
            Long neighborhoodId = dto.id();
            String name = dto.name();
            Long count = dto.count();

            ranking.add(new ConvenienceRankDto.NeighborhoodConvenienceRankDto(
                    i,
                    neighborhoodId,
                    name,
                    count
            ));
            i++;
        }

        return new ConvenienceRankDto(ranking);
    }

    public ConvenienceTypeCountDto getConvenienceTypeCount(Long neighborhoodId) {
        Neighborhood neighborhood = neighborhoodRepository.findById(neighborhoodId).orElseThrow(
                () -> new BaseException(ErrorCode.NEIGHBORHOOD_NOT_FOUND, "해당 id의 읍/면/동이 존재하지 않습니다."));

        return new ConvenienceTypeCountDto(
                neighborhood.getDistrict().getName() + " " + neighborhood.getName(),
                getNeighborhoodConvenienceTypeCountDto(neighborhoodId, ConvenienceType.MART),
                getNeighborhoodConvenienceTypeCountDto(neighborhoodId, ConvenienceType.CONVENIENCE_STORE),
                getNeighborhoodConvenienceTypeCountDto(neighborhoodId, ConvenienceType.HOSPITAL),
                getNeighborhoodConvenienceTypeCountDto(neighborhoodId, ConvenienceType.CAFE)
        );
    }

    /** ConvenienceType별 NeighborhoodConvenience 개수 및 NeighborhoodConvenience 반환 */
    private ConvenienceTypeCountDto.ConvenienceGroupDto getNeighborhoodConvenienceTypeCountDto(Long neighborhoodId, ConvenienceType convenienceType) {
        List<ConvenienceTypeCountDto.ConvenienceDto> list = neighborhoodConvenienceRepository.findByNeighborhood_IdAndType(neighborhoodId, convenienceType)
                .stream()
                .map(neighborhoodConvenience ->
                        new ConvenienceTypeCountDto.ConvenienceDto(
                                neighborhoodConvenience.getName(),
                                neighborhoodConvenience.getLatitude(),
                                neighborhoodConvenience.getLongitude()
                        )
                ).toList();

        return new ConvenienceTypeCountDto.ConvenienceGroupDto(
                list.size(),
                list
        );
    }

}
