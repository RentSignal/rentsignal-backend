package me.rentsignal.global.exception;

import lombok.extern.slf4j.Slf4j;
import me.rentsignal.global.response.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 커스텀 에러 (BaseException) 발생한 경우
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseResponse<?>> handleBaseException(BaseException e) {
        ErrorCode errorCode = e.getErrorCode();
        String errorMessage = e.getMessage();

        log.error(errorMessage);

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(BaseResponse.error(errorCode, errorMessage));
    }

    // RequestBody 검증 실패한 경우 (필수 입력 항목 누락 / 형식 오류)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException - " + e.getMessage());

        FieldError fieldError = e.getBindingResult().getFieldError();

        String message = "필수 입력 항목이 누락되었거나 잘못된 형식입니다.";

        // 입력 형식 오류 시
        if (fieldError != null) {
            String code = fieldError.getCode();

            if ("Pattern".equals(code) || "Email".equals(code) || "Size".equals(code)
                    || "Min".equals(code) || "Max".equals(code)) {
                message = fieldError.getDefaultMessage();
            }
        }

        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT_VALUE.getStatus())
                .body(BaseResponse.error(ErrorCode.INVALID_INPUT_VALUE, message));
    }

    // RequestParam 타입 변환 실패한 경우
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseResponse<?>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("MethodArgumentTypeMismatchException - " + e.getMessage());

        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT_VALUE.getStatus())
                .body(BaseResponse.error(ErrorCode.INVALID_INPUT_VALUE, "Request Param 타입 변환에 실패했습니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<?>> handleException(Exception e) {
        log.error("Exception - " + e.getMessage());
        return ResponseEntity.internalServerError()
                .body(BaseResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, "서버에서 예상치 못한 오류가 발생했습니다. - " + e.getMessage()));
    }

}
