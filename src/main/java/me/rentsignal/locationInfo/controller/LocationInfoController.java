package me.rentsignal.locationInfo.controller;

import lombok.RequiredArgsConstructor;
import me.rentsignal.global.response.BaseResponse;
import me.rentsignal.locationInfo.dto.ConsumerSentimentIndexDto;
import me.rentsignal.locationInfo.dto.CurrentRentIndexDto;
import me.rentsignal.locationInfo.dto.RentIndexChangeDto;
import me.rentsignal.locationInfo.entity.HousingType;
import me.rentsignal.locationInfo.service.ConsumerSentimentIndexService;
import me.rentsignal.locationInfo.service.RentIndexService;
import me.rentsignal.locationInfo.type.PeriodType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/info")
public class LocationInfoController {

    private final RentIndexService rentIndexService;
    private final ConsumerSentimentIndexService consumerSentimentIndexService;

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

}
