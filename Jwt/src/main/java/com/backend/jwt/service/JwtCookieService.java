package com.backend.jwt.service;

import com.backend.jwt.model.JwtConfig;
import com.backend.jwt.type.JwtType;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtCookieService {
    public static String PROFILE_ACTIVE;

    @Value("${spring.profiles.active}")
    public void setProfileActive(String value) {
        PROFILE_ACTIVE = value;
    }

    public String getAccessToken(Cookie[] cookies, JwtConfig config) {
        return getTokenFromCookie(cookies, config.getNames().getAccessToken()).orElse(null);
    }

    public String getRefreshToken(Cookie[] cookies, JwtConfig config) {
        return getTokenFromCookie(cookies, config.getNames().getRefreshToken()).orElse(null);
    }

    public String getAnonymousToken(Cookie[] cookies, JwtConfig config, JwtType jwtType) {
        return getTokenFromCookie(cookies, config.getNames().getAnonymousToken() + jwtType).orElse(null);
    }

    public void setCookie(HttpServletResponse response, String name, String value, int maxAgeInSeconds) {
        if (PROFILE_ACTIVE.equals("prod")) {
            response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(name, value)
                .maxAge(maxAgeInSeconds)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .build().toString());
        } else {
            response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(name, value)
                .maxAge(maxAgeInSeconds)
                .httpOnly(true)
                .sameSite("None")
                .secure(true)
                .path("/")
                .build().toString());
        }
    }

    public void removeCookie(HttpServletResponse response, String token) {
        setCookie(response, token, "", 0);
    }

    private Optional<String> getTokenFromCookie(Cookie[] cookies, String cookieName) {
        return getCookie(cookies, cookieName).map(Cookie::getValue);
    }

    private Optional<Cookie> getCookie(Cookie[] cookies, String name) {

        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return Optional.of(cookie);
                }
            }
        }
        return Optional.empty();
    }
}
