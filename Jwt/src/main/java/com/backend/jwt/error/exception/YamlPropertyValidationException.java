package com.backend.jwt.error.exception;

public class YamlPropertyValidationException extends RuntimeException {
    public YamlPropertyValidationException(String message) {
        super(message);
    }
}