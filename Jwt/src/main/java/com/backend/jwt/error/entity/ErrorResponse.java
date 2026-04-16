package com.backend.jwt.error.entity;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

public class ErrorResponse<T> extends ResponseEntity<T> {

    public ErrorResponse(HttpStatusCode status) {
        super(status);
    }

    public ErrorResponse(T body, HttpStatusCode status) {
        super(body, status);
    }

    public ErrorResponse(MultiValueMap<String, String> headers, HttpStatusCode status) {
        super(headers, status);
    }

    public ErrorResponse(T body, MultiValueMap<String, String> headers, int rawStatus) {
        super(body, headers, rawStatus);
    }

    public ErrorResponse(T body, MultiValueMap<String, String> headers, HttpStatusCode statusCode) {
        super(body, headers, statusCode);
    }

    public static EntityBuilder status(JwtErrorCode errorCode) {
        return new EntityBuilder(errorCode);
    }

    public static EntityBuilder status(String code, HttpStatus status) {
        return new EntityBuilder(code, status);
    }

    public static class EntityBuilder {
        private final String errorCode;
        private final HttpStatus status;

        public EntityBuilder(JwtErrorCode errorCode) {
            this.errorCode = errorCode.name();
            this.status = errorCode.getStatus();
        }

        public EntityBuilder(String errorCode, HttpStatus status) {
            this.errorCode = errorCode;
            this.status = status;
        }

        public ResponseEntity<ErrorEntityBody> body() {
            return body(null);
        }

        public ResponseEntity<ErrorEntityBody> body(Object o) {
            ErrorEntityBody errorEntityBody = new ErrorEntityBody(errorCode, o);

            return status(status).body(errorEntityBody);
        }
    }
}
