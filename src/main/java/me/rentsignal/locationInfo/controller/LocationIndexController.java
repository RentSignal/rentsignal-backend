package me.rentsignal.locationInfo.controller;

import lombok.RequiredArgsConstructor;
import me.rentsignal.global.response.BaseResponse;
import me.rentsignal.locationInfo.dto.*;
import me.rentsignal.locationInfo.entity.HousingType;
import me.rentsignal.locationInfo.service.ConsumerSentimentIndexService;
import me.rentsignal.locationInfo.service.RentIndexService;
import me.rentsignal.locationInfo.service.SubwayAccessibilityIndexService;
import me.rentsignal.locationInfo.type.PeriodType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/info")
public class LocationIndexController {

    private final RentIndexService rentIndexService;
    private final ConsumerSentimentIndexService consumerSentimentIndexService;
    private final SubwayAccessibilityIndexService subwayAccessibilityIndexService;

    @GetMapping("/rent-index/current")
    public ResponseEntity<?> getCurrentRentIndex(@RequestParam HousingType housingType) {
        CurrentRentIndexDto currentRentIndex = rentIndexService.getCurrentRentIndex(housingType);
        return ResponseEntity.ok().body(BaseResponse.success(currentRentIndex));
    }

    @GetMapping("/rent-index/change")
    public ResponseEntity<?> getRentIndexChange(@RequestParam HousingType housingType,
                                                    @RequestParam PeriodType periodType) {
        RentIndexChangeDto rentIndexChange = rentIndexService.getRentIndexChange(housingType, periodType);
        return ResponseEntity.ok().body(BaseResponse.success(rentIndexChange));
    }

    @GetMapping("/consumer-sentiment")
    public ResponseEntity<?> getConsumerSentimentIndex(@RequestParam PeriodType periodType) {
        ConsumerSentimentIndexDto consumerSentimentIndex = consumerSentimentIndexService.getConsumerSentimentIndex(periodType);
        return ResponseEntity.ok().body(BaseResponse.success(consumerSentimentIndex));
    }

    @GetMapping("/subway-accessibility")
    public ResponseEntity<?> getSubwayAccessibilityIndex() {
        SubwayAccessibilityIndexDto subwayAccessibilityIndex = subwayAccessibilityIndexService.getSubwayAccessibilityIndex();
        return ResponseEntity.ok().body(BaseResponse.success(subwayAccessibilityIndex));
    }

    @GetMapping("/subway-accessibility/{districtId}")
    public ResponseEntity<?> getSubwayStationByDistrict(@PathVariable Long districtId) {
        DistrictSubwayDto subwayStationByDistrict = subwayAccessibilityIndexService.getSubwayStationByDistrict(districtId);
        return ResponseEntity.ok().body(BaseResponse.success(subwayStationByDistrict));
    }

}
