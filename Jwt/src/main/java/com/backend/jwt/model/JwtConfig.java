package com.backend.jwt.model;

import com.backend.jwt.validation.YamlValidation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@YamlValidation
public class JwtConfig {
    private String endpoint;

    private JwtNames names;

    private JwtKeys keys;

    private JwtExpires expires;
}
