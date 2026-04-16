package com.resumemanager.domain.auth.service;

import com.backend.jwt.config.JwtProfilePathFinder;
import com.backend.jwt.model.Jwt;
import com.backend.jwt.model.JwtConfig;
import com.backend.jwt.service.JwtService;
import com.backend.jwt.type.JwtProfileType;
import com.backend.jwt.type.JwtType;
import com.resumemanager.core.error.entity.ApiException;
import com.resumemanager.core.error.type.ErrorCode;
import com.resumemanager.domain.auth.model.dto.request.TokenRefreshRequest;
import com.resumemanager.domain.auth.model.dto.response.TokenResponse;
import com.resumemanager.domain.auth.model.dto.response.UserInfoResponse;
import com.resumemanager.domain.user.entity.User;
import com.resumemanager.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final JwtService jwtService;
    private final JwtProfilePathFinder jwtProfilePathFinder;
    private final UserService userService;

    private JwtConfig getJwtConfig() {
        return jwtProfilePathFinder.getJwtConfig(JwtProfileType.USER);
    }

    public TokenResponse refreshToken(TokenRefreshRequest request) {
        JwtConfig jwtConfig = getJwtConfig();
        Jwt jwt = jwtService.parseJwt(jwtConfig, request.getRefreshToken());

        if (jwt.validate()) throw new ApiException(ErrorCode.INVALID_TOKEN);
        if (jwt.isExpired()) throw new ApiException(ErrorCode.EXPIRED_TOKEN);

        String userIdStr = jwtService.decryptSubject(jwt.getSubject(), jwt.getCreatedAt());
        Long userId = Long.parseLong(userIdStr);
        User user = userService.getUserById(userId);

        Jwt newAccess = jwtService.generateJwt(
                jwtConfig, String.valueOf(user.getId()),
                JwtType.ACCESS, JwtProfileType.USER,
                jwtConfig.getExpires().publishAccessTokenExpiry(), null);

        Jwt newRefresh = jwtService.generateJwt(
                jwtConfig, String.valueOf(user.getId()),
                JwtType.REFRESH, JwtProfileType.USER,
                jwtConfig.getExpires().publishRefreshTokenExpiry(), null);

        return new TokenResponse(newAccess.getToken(), newRefresh.getToken());
    }

    public UserInfoResponse getCurrentUser() {
        Long userId = getCurrentUserId();
        if (userId == null) throw new ApiException(ErrorCode.UNAUTHORIZED);
        User user = userService.getUserById(userId);
        return UserInfoResponse.from(user);
    }

    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        try {
            return Long.parseLong(auth.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
