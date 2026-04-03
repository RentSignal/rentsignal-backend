package me.rentsignal.data.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
/**
 * 법정동 코드 csv 파일의 한 행 표현
 */
public class LegalDongCsvRowDto {

    private final String code;

    private final String provinceName;

    private final String districtName;

    private final String neighborhoodName;

    private final String riName;

}
