package com.backend.jwt.error.exception;

import com.google.gson.Gson;
import com.backend.jwt.error.entity.JwtErrorCode;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class JwtException extends RuntimeException {
    private final JwtErrorCode errorCode;
    private final String body;

    public JwtException(JwtErrorCode errorCode) {
        super(errorCode.name(), null, false, false);
        this.errorCode = errorCode;
        this.body = new Gson().toJson("");
    }

    public JwtException(JwtErrorCode errorCode, Object body) {
        super(errorCode.name(), null, false, false);
        this.errorCode = errorCode;
        this.body = new Gson().toJson(body);
    }

    public Map<String, Object> toError(){
        Map<String, Object> map = new HashMap<>();
        map.put("code", this.errorCode.name());
        map.put("body", new Gson().fromJson(body, Object.class));
        return map;
    }

    @Override
    public String toString() {
        return "[" + errorCode + "]\r\n" + body;
    }
}