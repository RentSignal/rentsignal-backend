package me.rentsignal.locationInfo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 지하철 역간 소요시간
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"line_name", "from_station_id", "to_station_id"})
        }
)
public class SubwayTravelTime {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String lineName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_station_id")
    private NeighborhoodTransport fromStation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_station_id")
    private NeighborhoodTransport toStation;

    @Column(nullable = false)
    private int travelTimeSeconds;

    @Builder
    public SubwayTravelTime(String lineName, NeighborhoodTransport fromStation, NeighborhoodTransport toStation, int travelTime) {
        this.lineName = lineName;
        this.fromStation = fromStation;
        this.toStation = toStation;
        this.travelTimeSeconds = travelTime;
    }

}
