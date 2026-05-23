package me.rentsignal.locationInfo.controller;

import lombok.RequiredArgsConstructor;
import me.rentsignal.global.response.BaseResponse;
import me.rentsignal.locationInfo.dto.ConvenienceRankDto;
import me.rentsignal.locationInfo.dto.ConvenienceTypeCountDto;
import me.rentsignal.locationInfo.service.ConvenienceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/info")
public class LocationLivingFactorController {

    private final ConvenienceService convenienceService;

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

}
