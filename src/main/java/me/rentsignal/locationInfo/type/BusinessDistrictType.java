package me.rentsignal.locationInfo.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BusinessDistrictType {
    GBD_GANGNAM("강남역"),
    GBD_YEOKSAM("역삼역"),
    GBD_SAMSEONG("삼성역"),
    GBD_JAMSIL("잠실역"),
    YBD_YEOUIDO("여의도역"),
    YBD_YEOUINARU("여의나루역"),
    YBD_DANGSAN("당산역"),
    CBD_GWANGHWAMUN("광화문역"),
    CBD_CITYHALL("시청역"),
    CBD_JONGGAK("종각역");

    public final String name;
}
