package com.backend.jwt.model;

import com.backend.jwt.validation.YamlValidation;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@YamlValidation
public class JwtNames {
    private String accessToken;

    private String refreshToken;

    private String anonymousToken;
}
