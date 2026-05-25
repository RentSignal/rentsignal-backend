package me.rentsignal.locationInfo.service;

import lombok.RequiredArgsConstructor;
import me.rentsignal.global.exception.BaseException;
import me.rentsignal.global.exception.ErrorCode;
import me.rentsignal.location.entity.Neighborhood;
import me.rentsignal.locationInfo.dto.RecommendedNeighborhoodByBusinessDistrict;
import me.rentsignal.locationInfo.dto.SubwayReachableStationDto;
import me.rentsignal.locationInfo.entity.NeighborhoodTransport;
import me.rentsignal.locationInfo.entity.SubwayTravelTime;
import me.rentsignal.locationInfo.entity.TransportType;
import me.rentsignal.locationInfo.repository.NeighborhoodTransportRepository;
import me.rentsignal.locationInfo.repository.SubwayTravelTimeRepository;
import me.rentsignal.locationInfo.type.BusinessDistrictType;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TransportService {

    private static final int MAX_SECONDS = 20 * 60; // 20분

    private final NeighborhoodTransportRepository neighborhoodTransportRepository;
    private final SubwayTravelTimeRepository subwayTravelTimeRepository;

    /** 환승 제외 주요 업무지구까지 20분 이내 소요되는 지하철역 조회 */
    public List<RecommendedNeighborhoodByBusinessDistrict> getRecommendedNeighborhoodByBusinessDistrict(BusinessDistrictType businessDistrictType) {
        String neighborhoodTransportName = convertToNeighborhoodTransportName(businessDistrictType);
        String[] names = neighborhoodTransportName.split(",");

        Map<Long, NeighborhoodStationGroup> grouped = new HashMap<>();

        for (String name : names) {
            NeighborhoodTransport mainStation = neighborhoodTransportRepository.findByNameAndTransportType(name, TransportType.SUBWAY_STATION).orElseThrow(
                    () -> new BaseException(ErrorCode.NEIGHBORHOOD_TRANSPORT_NOT_FOUND, "해당 이름의 neighborhoodTransport가 존재하지 않습니다. - " + name));

            // 기준역까지 maxSeconds (20분) 내 도달 가능한 지하철역 찾기
            List<ReachableStationAndNeighborhood> reachableStationAndNeighborhoods = findReachableStations(mainStation.getId(), MAX_SECONDS);

            // neighborhood별로 그룹핑
            for (ReachableStationAndNeighborhood reachableStationAndNeighborhood : reachableStationAndNeighborhoods) {
                Neighborhood neighborhood = reachableStationAndNeighborhood.neighborhood();

                String[] arr = reachableStationAndNeighborhood.stationName().split("\\|");
                NeighborhoodStationGroup group = grouped.computeIfAbsent(
                        neighborhood.getId(),
                        key -> new NeighborhoodStationGroup(neighborhood)
                );

                group.stations().add(
                        new SubwayReachableStationDto(
                                arr[1],
                                arr[0],
                                reachableStationAndNeighborhood.seconds() / 60,
                                reachableStationAndNeighborhood.seconds() % 60
                        )
                );
            }
        }

        return grouped.values().stream()
                .map(group -> new RecommendedNeighborhoodByBusinessDistrict(
                        group.neighborhood().getId(),
                        group.neighborhood().getDistrict().getName().replace("시", "시 ") + " " + group.neighborhood().getName(),
                        group.stations()
                )).toList();
    }

    private String convertToNeighborhoodTransportName(BusinessDistrictType businessDistrictType) {
        return switch (businessDistrictType) {
            case GBD_GANGNAM -> "강남|2호선,강남|신분당선";
            case GBD_YEOKSAM -> "역삼|2호선";
            case GBD_JAMSIL -> "잠실(송파구청)|2호선,잠실(송파구청)|8호선";
            case GBD_SAMSEONG -> "삼성(무역센터)|2호선";
            case YBD_YEOUIDO -> "여의도|5호선,여의도|9호선";
            case YBD_YEOUINARU -> "여의나루|5호선";
            case YBD_DANGSAN -> "당산|2호선,당산|9호선";
            case CBD_GWANGHWAMUN -> "광화문(세종문화회관)|5호선";
            case CBD_CITYHALL -> "시청|1호선,시청|2호선";
            case CBD_JONGGAK -> "종각|1호선";
        };
    }

    private List<ReachableStationAndNeighborhood> findReachableStations(Long fromId, int maxSeconds) {
        List<SubwayTravelTime> all = subwayTravelTimeRepository.findAll();

        Map<Long, List<Edge>> graph = buildGraph(all);

        Map<Long, NeighborhoodTransport> neighborhoodTransportMap = loadNeighborhoodTransportMap(all);

        Map<Long, Integer> distances = dijkstra(fromId, graph, maxSeconds);

        return distances.entrySet().stream()
                .filter(entry -> entry.getValue() <= maxSeconds)
                .filter(entry -> !entry.getKey().equals(fromId))
                .map(entry -> {
                    NeighborhoodTransport subwayStation = neighborhoodTransportMap.get(entry.getKey());

                    if (subwayStation == null || subwayStation.getNeighborhood() == null) {
                        return null;
                    }

                    return new ReachableStationAndNeighborhood(
                            subwayStation.getName(),
                            subwayStation.getNeighborhood(),
                            entry.getValue()
                    );
                })
                .filter(Objects::nonNull)
                .toList();
    }

    /** 지하철역 노드로 이루어진 그래프 생성 */
    private Map<Long, List<Edge>> buildGraph(List<SubwayTravelTime> subwayTravelTimes) {
        Map<Long, List<Edge>> graph = new HashMap<>();

        for (SubwayTravelTime subwayTravelTime : subwayTravelTimes) {
            // 역 ID -> 해당 역에서 갈 수 있는 역 리스트
            Long fromId = subwayTravelTime.getFromStation().getId();
            Long toId = subwayTravelTime.getToStation().getId();
            int seconds = subwayTravelTime.getTravelTimeSeconds();

            // 양방향 그래프
            graph.computeIfAbsent(fromId, key -> new ArrayList<>())
                    .add(new Edge(toId, seconds));
            graph.computeIfAbsent(toId, key -> new ArrayList<>())
                    .add(new Edge(fromId, seconds));
        }

        return graph;
    }

    /** 다익스트라로 maxSeconds (20분) 이내 최단경로 계산 */
    private Map<Long, Integer> dijkstra(Long fromId, Map<Long, List<Edge>> graph, int maxSeconds) {
        // 각 역별로 기준 역에서 걸리는 최소시간
        Map<Long, Integer> distances = new HashMap<>();
        // 현재까지 발견한 역 중 이동시간이 가장 짧은 역 꺼냄
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(Node::seconds));

        distances.put(fromId, 0);
        pq.add(new Node(fromId, 0));

        while (!pq.isEmpty()) {
            // 1. 현재 가장 가까운 역
            Node current = pq.poll();

            // 2. 방금 PQ에서 꺼낸 후보 경로의 시간 > 실제 최단 시간이거나 최대 시간 오버할 경우 무시
            if (current.seconds() > distances.getOrDefault(current.stationId(), Integer.MAX_VALUE)
                    || current.seconds() > maxSeconds) {
                continue;
            }

            // 3. 현재 역과 연결된 인접역 확인
            for (Edge edge : graph.getOrDefault(current.stationId(), List.of())) {
                // 다음 인접역까지 총 소요시간
                int nextSeconds = current.seconds() + edge.seconds();

                if (nextSeconds > maxSeconds) {
                    continue;
                }

                // 다음 역까지의 기존 경로보다 더 짧을 경우 갱신
                if (nextSeconds < distances.getOrDefault(edge.toStationId(), Integer.MAX_VALUE)) {
                    distances.put(edge.toStationId(), nextSeconds);
                    pq.add(new Node(edge.toStationId(), nextSeconds));
                }
            }
        }

        return distances;
    }

    private Map<Long, NeighborhoodTransport> loadNeighborhoodTransportMap(List<SubwayTravelTime> subwayTravelTimes) {
        Map<Long, NeighborhoodTransport> neighborhoodTransportMap = new HashMap<>();

        for (SubwayTravelTime subwayTravelTime : subwayTravelTimes) {
            neighborhoodTransportMap.put(
                    subwayTravelTime.getFromStation().getId(),
                    subwayTravelTime.getFromStation()
            );

            neighborhoodTransportMap.put(
                    subwayTravelTime.getToStation().getId(),
                    subwayTravelTime.getToStation()
            );
        }

        return neighborhoodTransportMap;
    }

    private record NeighborhoodStationGroup(
            Neighborhood neighborhood,
            List<SubwayReachableStationDto> stations
    ) {
        private NeighborhoodStationGroup(Neighborhood neighborhood) {
            this(neighborhood, new ArrayList<>());
        }
    }

    private record ReachableStationAndNeighborhood(
            String stationName,
            Neighborhood neighborhood,
            int seconds
    ) {}

    private record Edge(
            Long toStationId,
            int seconds
    ) {}

    private record Node(
            Long stationId,
            int seconds
    ) {}

}
