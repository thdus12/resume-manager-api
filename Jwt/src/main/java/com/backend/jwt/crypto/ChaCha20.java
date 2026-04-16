package com.backend.jwt.crypto;

import com.backend.jwt.validation.YamlValidation;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@YamlValidation
@ConfigurationProperties("chacha20")
public class ChaCha20 {

    private String pepper;

    private String password;
}