package com.backend.jwt.service;

import com.backend.jwt.model.Jwt;
import com.backend.jwt.model.JwtConfig;
import com.backend.jwt.type.JwtProfileType;
import com.backend.jwt.type.JwtType;

import java.security.Key;

public interface JwtService {
    // JWT 토큰 생성
    Jwt generateJwt(JwtConfig config, String subject, JwtType type, JwtProfileType profileType, Long expiry, String code);

    // JWT 서명 키 생성
    Key getKey(JwtConfig config);

    // JWT 토큰 파싱
    Jwt parseJwt(JwtConfig config, String token);

    // Subject 암호화
    String encryptSubject(String subject);

    // Subject 암호화 (timestamp 지정)
    String encryptSubject(String subject, Long timestamp);

    // Subject 복호화
    String decryptSubject(String encryptedSubject, Long timestamp);
}