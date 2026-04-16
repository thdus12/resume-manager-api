package com.resumemanager.core.error.type;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    BAD_REQUEST(400, HttpStatus.BAD_REQUEST),
    DUPLICATE_EMAIL(400, HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(401, HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(401, HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN(401, HttpStatus.UNAUTHORIZED),
    LOGIN_FAILED(401, HttpStatus.UNAUTHORIZED),
    FORBIDDEN(403, HttpStatus.FORBIDDEN),
    NOT_FOUND(404, HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(404, HttpStatus.NOT_FOUND),
    RESUME_NOT_FOUND(404, HttpStatus.NOT_FOUND),
    INTERNAL_SERVER_ERROR(500, HttpStatus.INTERNAL_SERVER_ERROR),
    LLM_ERROR(500, HttpStatus.INTERNAL_SERVER_ERROR),
    ;

    private final int code;
    private final HttpStatus status;

    ErrorCode(final int code, final HttpStatus status) {
        this.code = code;
        this.status = status;
    }
}
