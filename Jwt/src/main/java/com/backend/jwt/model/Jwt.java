package com.backend.jwt.model;

import com.backend.jwt.type.JwtProfileType;
import com.backend.jwt.type.JwtType;
import io.jsonwebtoken.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
@Setter
public class Jwt {
    private final Key key;              // JWT 서명에 사용되는 키
    private String token;               // JWT 토큰 문자열
    private String subject;             // JWT 토큰 subject 정보
    private Claims claims;              // JWT 토큰 클레임 정보
    private Long expiry;                // JWT 토큰 만료 시간
    private Long createdAt;             // JWT 토큰 생성 시간
    private JwtType type;               // JWT 토큰 타입
    private JwtProfileType profileType; // JWT 토큰의 프로필 타입
    private String code;                // 인증 코드

    /**
     * JWT 토큰 문자열과 서명 키를 받아 파싱된 JWT 객체를 생성하는 생성자
     *
     * @param key   JWT 서명에 사용되는 키
     * @param token JWT 토큰 문자열
     */
    public Jwt(Key key, String token) {
        this.key = key;
        this.token = token;

        try {
            JwtParser parser = Jwts.parser().verifyWith((javax.crypto.SecretKey) key).build();
            this.claims = parser.parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            this.claims = e.getClaims();
        }

        this.expiry = this.claims.get("exp", Long.class);
        this.createdAt = this.claims.get("iat", Long.class);
        this.subject = claims.getSubject();

        Object claimsType = this.claims.get("type");
        Object claimsProfileType = this.claims.get("profileType");
        Object claimsCode = this.claims.get("code");

        this.type = claimsType != null ? JwtType.fromName(claimsType.toString()) : null;
        this.profileType = claimsProfileType != null ? JwtProfileType.fromName(claimsProfileType.toString()) : null;
        this.code = claimsCode != null ? claimsCode.toString() : null;
    }

    /**
     * JWT 토큰 생성을 위한 생성자
     *
     * @param key          JWT 서명에 사용되는 키
     * @param subject      JWT 토큰의 subject 정보
     * @param type         JWT 토큰의 타입
     * @param profileType  JWT 토큰의 프로필 타입
     * @param expiry       JWT 토큰의 만료 시간
     * @param code         인증 코드
     */
    public Jwt(Key key, String subject, JwtType type, JwtProfileType profileType, Long expiry, String code) {
        this(key, subject, type, profileType, expiry, code, System.currentTimeMillis() / 1000);
    }

    /**
     * JWT 토큰 생성을 위한 생성자 (timestamp 지정)
     *
     * @param key          JWT 서명에 사용되는 키
     * @param subject      JWT 토큰의 subject 정보
     * @param type         JWT 토큰의 타입
     * @param profileType  JWT 토큰의 프로필 타입
     * @param expiry       JWT 토큰의 만료 시간
     * @param code         인증 코드
     * @param timestamp    JWT 생성 시간 (초 단위)
     */
    public Jwt(Key key, String subject, JwtType type, JwtProfileType profileType, Long expiry, String code, Long timestamp) {
        this.key = key;
        Date issuedAt = new Date(timestamp * 1000);
        this.subject = subject;
        this.createdAt = timestamp;
        this.type = type;
        this.profileType = profileType;
        this.expiry = expiry;
        this.code = code;

        this.token = Jwts.builder()
            .subject(subject)
            .issuedAt(issuedAt)
            .expiration(new Date(expiry))
            .claim("type", type)
            .claim("profileType", profileType)
            .claim("code", code)
            .signWith((javax.crypto.SecretKey) key)
            .compact();

        this.claims = Jwts.parser().verifyWith((javax.crypto.SecretKey) key).build().parseSignedClaims(this.token).getPayload();
    }

    /**
     * JWT 토큰 유효성 검증
     *
     * @return JWT 토큰이 유효하지 않으면 true, 유효하면 false 를 반환
     */
    public boolean validate() {
        return this.token == null || this.claims == null;
    }

    /**
     * JWT 토큰 만료 확인
     *
     * @return JWT 토큰이 만료되었으면 true, 만료되지 않았으면 false 를 반환a
     */
    public boolean isExpired() {
        try {
            JwtParser jwtParser = Jwts.parser().verifyWith((javax.crypto.SecretKey) key).build();

            Jws<Claims> jws = jwtParser.parseSignedClaims(token);
            Claims jwsClaims = jws.getPayload();

            long expirationTime = jwsClaims.get("exp", Long.class);
            long currentTime = System.currentTimeMillis() / 1000;

            return expirationTime < currentTime;
        } catch (JwtException e) {
            // 토큰이 유효하지않거나 파싱할수 없을 경우
            log.error(e.getMessage());
            return true;
        }
    }

    public boolean isExpired(Duration duration) {
        // 생성 시간 부터 duration만큼 시간이 지났는지 체크
        long expirationTime = this.createdAt + duration.getSeconds();
        long currentTime = System.currentTimeMillis() / 1000;

        return currentTime > expirationTime;
    }

    @Override
    public String toString() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", key.getFormat());
        map.put("token", token);
        map.put("encryptedSubject", subject);
        Map<String, Object> c = new HashMap<>(claims);
        map.put("claims", c);
        map.put("expiry", expiry);
        map.put("code", code);
        return new com.google.gson.Gson().toJson(map);
    }
}