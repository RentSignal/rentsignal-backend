package me.rentsignal.data.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.io.ParseException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * NeighborhoodTransport를 위치한 읍면동 (Neighborhood)에 매핑
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransportNeighborhoodMappingService {

    private final JdbcTemplate jdbcTemplate;
    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), 4326);
    private final Double MAX_DISTANCE = 0.004;

    public int mapTransportNeighborhood() {
        // JTS 공간 인덱스 생성
        BoundaryIndex boundaryIndex = loadBoundaryIndex();

        long lastId = 0L;
        int total = 0;

        while (true) {
            BatchResult result = mapBatch(lastId, boundaryIndex);

            if (result.processedCount() == 0) {
                break;
            }

            lastId = result.lastId();
            total += result.updatedCount();

            log.info("마지막 정류장/역 id = " + lastId + ", 총 업데이트 수 = " + total);
        }

        return total;
    }

    /** 5000개씩 매핑 */
    @Transactional
    public BatchResult mapBatch(long lastId, BoundaryIndex boundaryIndex) {
        // 아직 매핑되지 않은 transport 5000개 조회
        List<TransportPoint> transports = jdbcTemplate.query(
                """
                SELECT id, longitude, latitude
                FROM neighborhood_transport
                WHERE id > ?
                  AND neighborhood_id IS NULL
                ORDER BY id
                LIMIT 5000
                """,
                (rs, rowNum) -> new TransportPoint(
                        rs.getLong("id"),
                        rs.getDouble("longitude"),
                        rs.getDouble("latitude")
                ),
                lastId
        );

        if (transports.isEmpty()) {
            return new BatchResult(0, 0, lastId);
        }

        List<MappingResult> results = new ArrayList<>();

        for (TransportPoint t : transports) {
            // 위도/경도 -> JTS Point 생성
            Point point = geometryFactory.createPoint(
                    new Coordinate(t.longitude(), t.latitude())  // (경도, 위도)
            );

            // 1차 - Point가 속한 읍면동에 조회
            Long neighborhoodId = boundaryIndex.findNeighborhoodId(point);

            // 2차 - 속해있는 읍면동을 찾지 못한 경우 가까운 (300m 이내) 읍면동에 매핑
            if (neighborhoodId == null) {
                neighborhoodId = boundaryIndex.findNearestNeighborhoodId(point);
            }

            if (neighborhoodId != null) {
                results.add(new MappingResult(t.id(), neighborhoodId));
            } else {
                log.warn("읍면동 매핑에 실패했습니다. - id = {}, lat = {}, lng = {}", t.id(), t.latitude(), t.longitude());
            }
        }

        if (!results.isEmpty()) {
            jdbcTemplate.batchUpdate(
                    """
                    UPDATE neighborhood_transport
                    SET neighborhood_id = ?
                    WHERE id = ?
                    """,
                    results,
                    5000,
                    (ps, result) -> {
                        ps.setLong(1, result.neighborhoodId());
                        ps.setLong(2, result.transportId());
                    }
            );
        }

        long newLastId = transports.get(transports.size() - 1).id();

        return new BatchResult(transports.size(), results.size(), newLastId);
    }

    /**
     * DB에 있는 boundary polygon 가져와서 JTS Geometry로 변환
     * -> 좌표가 속할 수 있는 boundary 후보 빠르게 조회하기 위한 Boundary Index 생성
     */
    private BoundaryIndex loadBoundaryIndex() {
        // JTS 공간 인덱스 (boundary 후보 빠르게 찾기 위한 R-tree)
        STRtree tree = new STRtree();
        // DB의 WKB(이진 데이터) -> JTS Geometry 객체로 변환하기 위한 reader
        WKBReader reader = new WKBReader(geometryFactory);

        List<Boundary> boundaries = jdbcTemplate.query(
                """
                SELECT neighborhood_id, ST_AsWKB(ST_SwapXY(geom)) AS geom_wkb
                FROM neighborhood_boundary
                """,
                (rs, rowNum) -> {
                    Long neighborhoodId = rs.getLong("neighborhood_id");
                    byte[] wkb = rs.getBytes("geom_wkb");

                    Geometry geometry = null;
                    try {
                        geometry = reader.read(wkb);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    PreparedGeometry prepared = PreparedGeometryFactory.prepare(geometry);

                    return new Boundary(neighborhoodId, geometry, prepared);
                }
        );

        // 공간 인덱스에 Boundary 삽입
        for (Boundary boundary : boundaries) {
            tree.insert(boundary.geometry().getEnvelopeInternal(), boundary);
        }

        tree.build();

        return new BoundaryIndex(tree);
    }

    /** boundary polygon 기반으로 좌표 (Point)가 속한 읍면동을 찾기 위한 인덱스 클래스  */
    private class BoundaryIndex {

        private final STRtree tree;

        private BoundaryIndex(STRtree tree) {
            this.tree = tree;
        }

        /** 좌표가 속하는 읍면동 조회 */
        Long findNeighborhoodId(Point point) {
            List<Boundary> candidates = tree.query(point.getEnvelopeInternal());

            for (Boundary boundary : candidates) {
                if (boundary.preparedGeometry().covers(point)) {
                    return boundary.neighborhoodId();
                }
            }

            return null;
        }

        /** 가장 가까운 읍면동 조회 */
        Long findNearestNeighborhoodId(Point point) {
            // 좌표와 MAX_DISTANCE 거리 내의 읍면동 조회
            List<Boundary> candidates = tree.query(point.buffer(MAX_DISTANCE).getEnvelopeInternal());

            Long nearestId = null;
            Double nearestDistance = Double.MAX_VALUE;

            // 가장 가까운 읍면동 찾기
            for (Boundary boundary : candidates) {
                double distance = boundary.geometry().distance(point);

                if (distance < nearestDistance) {
                    nearestId = boundary.neighborhoodId();
                    nearestDistance = distance;
                }
            }

            if (nearestDistance <= MAX_DISTANCE) {
                return nearestId;
            }

            return null;
        }

    }

    // 읍면동 경계 정보
    private record Boundary(
            Long neighborhoodId,
            Geometry geometry,  // 실제 polygon
            PreparedGeometry preparedGeometry  // 연산 최적화를 위한 Geometry
    ) {}

    private record BatchResult(
            int processedCount,
            int updatedCount,
            long lastId
    ) {}

    private record TransportPoint(
            Long id,
            Double longitude,
            Double latitude
    ) {}

    private record MappingResult(
            Long transportId,  // neighborhood_transport id
            Long neighborhoodId
    ) {}

}
