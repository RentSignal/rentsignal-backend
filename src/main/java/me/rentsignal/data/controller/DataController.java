package me.rentsignal.data.controller;

import lombok.RequiredArgsConstructor;
import me.rentsignal.data.service.*;
import me.rentsignal.global.response.BaseResponse;
import me.rentsignal.locationInfo.entity.HousingType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class DataController {

    private final LegalDongImportService legalDongImportService;
    private final RegionDataService regionDataService;
    private final RentIndexDataService rentIndexDataService;
    private final SentimentIndexDataService sentimentIndexDataService;
    private final SubwayIndexDataService subwayIndexDataService;

    @PostMapping("/legal-dong")
    public ResponseEntity<?> saveLegalDong() {
        legalDongImportService.importLegalDongCsv();
        return ResponseEntity.ok().body(BaseResponse.success(null));
    }

    @PostMapping("/region")
    public ResponseEntity<?> saveRegion() {
        regionDataService.saveRegion();
        return ResponseEntity.ok().body(BaseResponse.success(null));
    }

    @PostMapping("/rent-index")
    public ResponseEntity<?> saveRentIndex(@RequestParam HousingType housingType) {
        rentIndexDataService.saveRentCompositeIndex(housingType);
        return ResponseEntity.ok().body(BaseResponse.success(null));
    }

    @PostMapping("/consumer-sentiment-index")
    public ResponseEntity<?> saveConsumerSentimentIndex() {
        sentimentIndexDataService.saveConsumerSentimentIndex();
        return ResponseEntity.ok().body(BaseResponse.success(null));
    }

    @PostMapping("/subway-accessibility-index")
    public ResponseEntity<?> saveSubwayAccessibilityIndex() {
        subwayIndexDataService.saveSubwayAccessibilityIndex();
        return ResponseEntity.ok().body(BaseResponse.success(null));
    }

}
