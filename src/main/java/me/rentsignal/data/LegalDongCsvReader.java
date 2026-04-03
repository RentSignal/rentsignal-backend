package me.rentsignal.data;

import lombok.extern.slf4j.Slf4j;
import me.rentsignal.data.dto.LegalDongCsvRowDto;
import me.rentsignal.global.exception.BaseException;
import me.rentsignal.global.exception.ErrorCode;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class LegalDongCsvReader {

    /** csv 파일의 행 -> LegalDongCsvRowDto로 변환 */
    public List<LegalDongCsvRowDto> read() {
        List<LegalDongCsvRowDto> rows = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            new ClassPathResource("legal-dong-code.csv").getInputStream(), StandardCharsets.UTF_8
                    )
            );

            String line;
            Boolean isFirst = true;

            while ((line = br.readLine()) != null) {
                // csv 파일 맨 윗줄 헤더 스킵
                if (isFirst) {
                    isFirst = false;
                    continue;
                }

                // 데이터 형식 - 법정동 코드 / 시도명 / 시군구명 / 읍면동명 / 리명 / 순위 / 생성일자
                String[] tokens = line.split(",", -1);

                String code = get(tokens, 0);
                String provinceName = get(tokens, 1);
                String districtName = get(tokens, 2);
                String neighborhoodName = get(tokens, 3);
                String riName = get(tokens, 4);

                rows.add(
                        LegalDongCsvRowDto.builder()
                                .code(code).provinceName(provinceName)
                                .districtName(districtName).neighborhoodName(neighborhoodName)
                                .riName(riName).build());

            }
        } catch (IOException e) {
            log.error("데이터 로드 실패" + e.getMessage());
            throw new BaseException(ErrorCode.CSV_READ_ERROR, "법정동 코드 csv 파일 읽기에 실패했습니다. - " + e.getMessage());
        }
        return rows;
    }

    private String get(String[] tokens, int index) {
        if (tokens.length <= index) // 데이터 깨짐 방지
            return "";
        return tokens[index].trim();
    }

}
