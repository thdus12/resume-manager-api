package com.backend.jwt.model;

import com.backend.jwt.validation.YamlValidation;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@YamlValidation
@ConfigurationProperties("jwt")
public class JwtProfiles {
    private Map<String, JwtConfig> profiles;

    private List<String> allowPaths;

    public JwtConfig getProfile(String profileName) {
        return profiles.get(profileName);
    }
}
