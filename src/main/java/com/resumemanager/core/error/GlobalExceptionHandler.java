package com.resumemanager.core.error;

import com.resumemanager.core.error.entity.ApiException;
import com.resumemanager.core.error.type.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    protected ResponseEntity<Map<String, Object>> handleApiException(ApiException e) {
        log.error("ApiException - {}", e.getErrorCode().name());
        String message = e.getDetail() != null ? e.getDetail() : e.getErrorCode().name();
        return ResponseEntity.status(e.getErrorCode().getStatus())
                .body(Map.of("code", e.getErrorCode().name(), "message", message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Map<String, Object>> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return ResponseEntity.badRequest()
                .body(Map.of("code", "BAD_REQUEST", "message", message));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(Map.of("code", "INTERNAL_SERVER_ERROR", "message", "서버 오류가 발생했습니다"));
    }
}
