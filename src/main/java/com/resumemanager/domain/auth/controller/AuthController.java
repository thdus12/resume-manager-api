package com.resumemanager.domain.auth.controller;

import com.resumemanager.domain.auth.model.dto.request.TokenRefreshRequest;
import com.resumemanager.domain.auth.model.dto.response.TokenResponse;
import com.resumemanager.domain.auth.model.dto.response.UserInfoResponse;
import com.resumemanager.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> me() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }
}
