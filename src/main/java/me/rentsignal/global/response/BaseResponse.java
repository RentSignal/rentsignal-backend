package me.rentsignal.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.rentsignal.global.exception.ErrorCode;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponse<T> {

    private boolean success;
    private String code;
    private String message;
    private T data;

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(true, "OK", "요청이 성공적으로 처리되었습니다.", data);
    }

    public static <T> BaseResponse<T> success(String message, T data) {
        return new BaseResponse<>(true, "OK", message, data);
    }

    public static <T> BaseResponse<T> error(ErrorCode code, String message) {
        return new BaseResponse<>(false, code.name(), message, null);
    }

}
