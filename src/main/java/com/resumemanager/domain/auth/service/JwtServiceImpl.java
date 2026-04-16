package com.resumemanager.domain.auth.service;

import com.backend.jwt.model.Jwt;
import com.backend.jwt.model.JwtConfig;
import com.backend.jwt.service.ChaCha20Service;
import com.backend.jwt.service.JwtService;
import com.backend.jwt.type.JwtProfileType;
import com.backend.jwt.type.JwtType;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final ChaCha20Service chaCha20Service;

    @Override
    public Jwt generateJwt(JwtConfig config, String subject, JwtType type, JwtProfileType profileType, Long expiry, String code) {
        Key key = getKey(config);
        Long timestamp = System.currentTimeMillis() / 1000;
        String encryptedSubject = encryptSubject(subject, timestamp);
        return new Jwt(key, encryptedSubject, type, profileType, expiry, code, timestamp);
    }

    @Override
    public Key getKey(JwtConfig config) {
        byte[] keyBytes = Base64.getDecoder().decode(config.getKeys().getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public Jwt parseJwt(JwtConfig config, String token) {
        Key key = getKey(config);
        return new Jwt(key, token);
    }

    @Override
    public String encryptSubject(String subject) {
        Long timestamp = System.currentTimeMillis() / 1000;
        return chaCha20Service.encrypt(subject, timestamp);
    }

    @Override
    public String encryptSubject(String subject, Long timestamp) {
        return chaCha20Service.encrypt(subject, timestamp);
    }

    @Override
    public String decryptSubject(String encryptedSubject, Long timestamp) {
        return chaCha20Service.decrypt(encryptedSubject, timestamp);
    }
}
