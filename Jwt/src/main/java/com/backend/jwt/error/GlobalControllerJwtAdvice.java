package com.backend.jwt.error;

import com.backend.jwt.error.exception.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice(basePackages = "com.backend.jwt")
public class GlobalControllerJwtAdvice {
    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> jwtExceptionHandler(JwtException e) {
        log.error("jwt exception", e);
        return e.toError();
    }
}
