package com.backend.jwt.service;

import com.backend.jwt.crypto.ChaCha20;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChaCha20Service {
    private final ChaCha20 chaCha20;

    private String saltDerivation(Long iat) {
        return new String((iat + chaCha20.getPepper()).substring(0, 64).getBytes());
    }

    public String encrypt(String plainText, Long iat) {
        String salt = saltDerivation(iat);
        return Encryptors.text(chaCha20.getPassword(), salt).encrypt(plainText);
    }

    public String decrypt(String encryptedText, Long iat) {
        String salt = saltDerivation(iat);
        return Encryptors.text(chaCha20.getPassword(), salt).decrypt(encryptedText);
    }
}