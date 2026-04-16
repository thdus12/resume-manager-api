package com.resumemanager.core.error.entity;

import com.resumemanager.core.error.type.ErrorCode;
import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String detail;

    public ApiException(ErrorCode errorCode) {
        super(errorCode.name(), null, false, false);
        this.errorCode = errorCode;
        this.detail = null;
    }

    public ApiException(ErrorCode errorCode, String detail) {
        super(errorCode.name(), null, false, false);
        this.errorCode = errorCode;
        this.detail = detail;
    }
}
