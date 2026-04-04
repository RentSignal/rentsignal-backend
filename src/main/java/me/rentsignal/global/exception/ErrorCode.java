package me.rentsignal.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 인증 관련
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED,  "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED,"만료된 토큰입니다."),
    USER_MISMATCH_INTERNAL(HttpStatus.INTERNAL_SERVER_ERROR, "사용자와 토큰의 매핑이 옳지 않습니다."),
    INVALID_PRINCIPAL(HttpStatus.INTERNAL_SERVER_ERROR, "유효하지 않은 인증 주체입니다."),
    TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND,  "토큰을 찾을 수 없습니다."),
    INVALID_SOCIAL_PROVIDER(HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 로그인입니다."),

    // 유저 관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),
    SOCIAL_ACCOUNT_NOT_OWNED(HttpStatus.INTERNAL_SERVER_ERROR, "사용자가 해당 소셜 계정을 보유하고 있지 않습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // 외부 API 관련
    EXTERNAL_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "외부 API에서 오류가 발생했습니다."),

    // 데이터 관련
    CSV_READ_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CSV 읽기에 실패했습니다."),
    INVALID_HOUSING_TYPE(HttpStatus.NOT_FOUND, "잘못된 housing type입니다."),
    DUPLICATED_DATA(HttpStatus.CONFLICT, "이미 존재하는 데이터입니다."),

    // 지역 관련
    PROVINCE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 시/도를 찾을 수 없습니다."),
    DISTRICT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 시/군/구를 찾을 수 없습니다."),
    NEIGHBORHOOD_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 읍/면/동을 찾을 수 없습니다."),
    RI_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 리를 찾을 수 없습니다."),
    REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 권역을 찾을 수 없습니다."),

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

}
