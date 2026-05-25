package me.rentsignal.locationInfo.controller;

import lombok.RequiredArgsConstructor;
import me.rentsignal.global.response.BaseResponse;
import me.rentsignal.locationInfo.dto.*;
import me.rentsignal.locationInfo.service.ConvenienceService;
import me.rentsignal.locationInfo.service.SafetyService;
import me.rentsignal.locationInfo.service.TransportService;
import me.rentsignal.locationInfo.type.BusinessDistrictType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/info")
public class LocationLivingFactorController {

    private final ConvenienceService convenienceService;
    private final SafetyService safetyService;
    private final TransportService transportService;

    @GetMapping("/convenience")
    public ResponseEntity<?> getConvenienceRanking() {
        ConvenienceRankDto convenienceRanking = convenienceService.getConvenienceRanking();
        return ResponseEntity.ok().body(BaseResponse.success(convenienceRanking));
    }

    @GetMapping("/convenience/{neighborhoodId}")
    public ResponseEntity<?> getConvenienceRanking(@PathVariable Long neighborhoodId) {
        ConvenienceTypeCountDto convenienceTypeCount = convenienceService.getConvenienceTypeCount(neighborhoodId);
        return ResponseEntity.ok().body(BaseResponse.success(convenienceTypeCount));
    }

    @GetMapping("/safety")
    public ResponseEntity<?> getSafety() {
        DistrictSafetyDto safety = safetyService.getSafety();
        return ResponseEntity.ok().body(BaseResponse.success(safety));
    }

    @GetMapping("/transport")
    public ResponseEntity<?> getTransport(@RequestParam BusinessDistrictType type) {
        List<RecommendedNeighborhoodByBusinessDistrict> recommendedNeighborhoodByBusinessDistrict = transportService.getRecommendedNeighborhoodByBusinessDistrict(type);
        return ResponseEntity.ok().body(BaseResponse.success(recommendedNeighborhoodByBusinessDistrict));
    }

    @GetMapping("/transport/{neighborhoodId}")
    public ResponseEntity<?> getNeighborhoodTransport(@PathVariable Long neighborhoodId) {
        NeighborhoodTransportDto neighborhoodTransport = transportService.getNeighborhoodTransport(neighborhoodId);
        return ResponseEntity.ok().body(BaseResponse.success(neighborhoodTransport));
    }

}
