package me.rentsignal.locationInfo.service;

import lombok.RequiredArgsConstructor;
import me.rentsignal.locationInfo.dto.ConvenienceRankDto;
import me.rentsignal.locationInfo.dto.NeighborhoodConvenienceQueryDto;
import me.rentsignal.locationInfo.repository.NeighborhoodConvenienceRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConvenienceService {

    private final NeighborhoodConvenienceRepository neighborhoodConvenienceRepository;

    public ConvenienceRankDto getConvenienceRanking() {
        List<NeighborhoodConvenienceQueryDto> topNeighborhoodConvenienceCount = neighborhoodConvenienceRepository.findTopNeighborhoodConvenienceCount(PageRequest.of(0, 7));

        List<ConvenienceRankDto.NeighborhoodConvenienceCountDto> ranking = new ArrayList<>();
        int i = 1;
        for (NeighborhoodConvenienceQueryDto dto : topNeighborhoodConvenienceCount) {
            Long neighborhoodId = dto.id();
            String name = dto.name();
            Long count = dto.count();

            ranking.add(new ConvenienceRankDto.NeighborhoodConvenienceCountDto(
                    i,
                    neighborhoodId,
                    name,
                    count
            ));
            i++;
        }

        return new ConvenienceRankDto(ranking);
    }

}
