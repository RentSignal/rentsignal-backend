package me.rentsignal.data.controller;

import lombok.RequiredArgsConstructor;
import me.rentsignal.data.service.ConvenienceStoreDataService;
import me.rentsignal.global.response.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class LifeStyleFactorDataCollector {

    private final ConvenienceStoreDataService convenienceStoreDataService;

    @PostMapping("/convenience-store")
    public ResponseEntity<?> saveConvenienceStore() {
        convenienceStoreDataService.saveConvenienceStore();
        return ResponseEntity.ok().body(BaseResponse.success(null));
    }

}
