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
    private final RentIndexService rentIndexService;
    private final SentimentIndexService sentimentIndexService;
    private final SubwayIndexService subwayIndexService;

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
        rentIndexService.saveRentCompositeIndex(housingType);
        return ResponseEntity.ok().body(BaseResponse.success(null));
    }

    @PostMapping("/consumer-sentiment-index")
    public ResponseEntity<?> saveConsumerSentimentIndex() {
        sentimentIndexService.saveConsumerSentimentIndex();
        return ResponseEntity.ok().body(BaseResponse.success(null));
    }

    @PostMapping("/subway-accessibility-index")
    public ResponseEntity<?> saveSubwayAccessibilityIndex() {
        subwayIndexService.saveSubwayAccessibilityIndex();
        return ResponseEntity.ok().body(BaseResponse.success(null));
    }

}
