package me.rentsignal.data.controller;

import lombok.RequiredArgsConstructor;
import me.rentsignal.data.service.*;
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
    private final NeighborhoodBoundaryDataService neighborhoodBoundaryDataService;
    private final BusStopDataImportService busStopDataImportService;
    private final TransportNeighborhoodMappingService transportNeighborhoodMappingService;
    private final SubwayDataService subwayDataService;

    @PostMapping("/convenience-store")
    public ResponseEntity<?> saveConvenienceStore() {
        convenienceStoreDataService.saveConvenienceStore();
        return ResponseEntity.ok().body(BaseResponse.success(null));
    }

    @PostMapping("/neighborhood-boundary")
    public ResponseEntity<?> saveNeighborhoodBoundary() {
        neighborhoodBoundaryDataService.saveNeighborhoodBoundaries();
        return ResponseEntity.ok().body(BaseResponse.success(null));
    }

    @PostMapping("/bus-stop")
    public ResponseEntity<?> saveBusStop() {
        busStopDataImportService.importBusStopCsv();
        return ResponseEntity.ok().body(BaseResponse.success(null));
    }

    @PostMapping("/bus-stop-neighborhood-map")
    public ResponseEntity<?> mapTransportNeighborhood() {
        int total = transportNeighborhoodMappingService.mapTransportNeighborhood();
        return ResponseEntity.ok().body(BaseResponse.success(total));
    }

    @PostMapping("/subway")
    public ResponseEntity<?> saveAndMapSubway() {
        int total = subwayDataService.importAndMapSubwayCsv();
        return ResponseEntity.ok().body(BaseResponse.success(total));
    }

}
