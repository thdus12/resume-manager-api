package com.backend.jwt.error.entity;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum JwtErrorCode {
    NONE(HttpStatus.OK),

    // JWT 에러
    JWT_UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
    JWT_TOKEN_INVALID(HttpStatus.UNAUTHORIZED),
    JWT_USER_DISABLED(HttpStatus.UNAUTHORIZED),

    // JWT 설정 에러
    JWT_CONFIG_NOT_FOUND(HttpStatus.BAD_REQUEST),
    JWT_PROFILE_NOT_FOUND(HttpStatus.BAD_REQUEST),
    JWT_PROFILE_EMPTY(HttpStatus.BAD_REQUEST),
    JWT_CONFIG_EMPTY(HttpStatus.BAD_REQUEST),
    JWT_CONFIG_IS_NULL(HttpStatus.BAD_REQUEST),
    JWT_VALIDATE_ERROR(HttpStatus.BAD_REQUEST),

    // Chacha20 설정 에러
    CHACHA20_VALIDATE_ERROR(HttpStatus.UNAUTHORIZED),
    CHACHA20_CONFIG_EMPTY(HttpStatus.UNAUTHORIZED),

    ;
    private HttpStatus status;

    JwtErrorCode(final HttpStatus status) {
        this.status = status;
    }
}

