package com.backend.jwt.model;

import com.backend.jwt.validation.YamlValidation;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@YamlValidation
public class JwtKeys {
    // Mail 전용 Secret Key
    private String anonymousSecret;

    // JWT Secret Key
    private String secret;
}
