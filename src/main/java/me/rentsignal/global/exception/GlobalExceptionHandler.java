package me.rentsignal.global.exception;

import lombok.extern.slf4j.Slf4j;
import me.rentsignal.global.response.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

}
