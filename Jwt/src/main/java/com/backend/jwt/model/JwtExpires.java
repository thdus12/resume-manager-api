package com.backend.jwt.model;

import com.backend.jwt.validation.YamlValidation;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.Date;

@Setter
@Getter
@YamlValidation
public class JwtExpires {
    // Access Token 만료 기간
    private Duration accessTokenExpiry;

    // Refresh Token 만료 기간
    private Duration refreshTokenExpiry;

    // Anonymous Token 만료 기간
    private Duration anonymousTokenExpiry;

    public Long publishAccessTokenExpiry() {

        return new Date(new Date().getTime() + accessTokenExpiry.toMillis()).getTime();
    }

    public Long publishRefreshTokenExpiry() {
        return new Date(new Date().getTime() + refreshTokenExpiry.toMillis()).getTime();
    }

    public Long publishAnonymousTokenExpiry() {
        return new Date(new Date().getTime() + anonymousTokenExpiry.toMillis()).getTime();
    }

    public int refreshTokenCookieMaxAge() {
        return (int) (refreshTokenExpiry.toMinutes() * 60);
    }

    public int accessTokenCookieMaxAge() {
        return (int) (accessTokenExpiry.toMinutes() * 60);
    }

    public int anonymousTokenCookieMaxAge() {
        return (int) (anonymousTokenExpiry.toMinutes() * 60);
    }
}
