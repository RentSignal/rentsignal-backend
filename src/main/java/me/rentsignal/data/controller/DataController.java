package me.rentsignal.data.controller;

import lombok.RequiredArgsConstructor;
import me.rentsignal.data.service.DataService;
import me.rentsignal.data.service.IndexService;
import me.rentsignal.data.service.LegalDongImportService;
import me.rentsignal.global.response.BaseResponse;
import me.rentsignal.global.security.CustomPrincipal;
import me.rentsignal.locationInfo.entity.HousingType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class DataController {

    private final LegalDongImportService legalDongImportService;
    private final DataService dataService;
    private final IndexService indexService;

    @PostMapping("/legal-dong")
    public ResponseEntity<?> saveLegalDong(@AuthenticationPrincipal CustomPrincipal customPrincipal) {
        legalDongImportService.importLegalDongCsv(customPrincipal.getId());
        return ResponseEntity.ok().body(BaseResponse.success(null));
    }

    @PostMapping("/region")
    public ResponseEntity<?> saveRegion(@AuthenticationPrincipal CustomPrincipal customPrincipal) {
        dataService.saveRegion(customPrincipal.getId());
        return ResponseEntity.ok().body(BaseResponse.success(null));
    }

    @PostMapping("/rent-index")
    public ResponseEntity<?> saveRentIndex(@AuthenticationPrincipal CustomPrincipal customPrincipal,
                                           @RequestParam HousingType housingType) {
        indexService.saveRentCompositeIndex(customPrincipal.getId(), housingType);
        return ResponseEntity.ok().body(BaseResponse.success(null));
    }

}
