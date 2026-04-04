package me.rentsignal.data.controller;

import lombok.RequiredArgsConstructor;
import me.rentsignal.data.service.DataService;
import me.rentsignal.data.service.LegalDongImportService;
import me.rentsignal.global.response.BaseResponse;
import me.rentsignal.global.security.CustomPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class DataController {

    private final LegalDongImportService legalDongImportService;
    private final DataService dataService;

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

}
