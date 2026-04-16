package com.resumemanager.core.config.security;

import com.backend.jwt.config.JwtProfilePathFinder;
import com.backend.jwt.model.Jwt;
import com.backend.jwt.model.JwtConfig;
import com.backend.jwt.service.JwtService;
import com.backend.jwt.type.JwtProfileType;
import com.backend.jwt.type.JwtType;
import com.resumemanager.domain.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final JwtProfilePathFinder jwtProfilePathFinder;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2UserPrincipal principal = (OAuth2UserPrincipal) authentication.getPrincipal();
        User user = principal.getUser();

        JwtConfig jwtConfig = jwtProfilePathFinder.getJwtConfig(JwtProfileType.USER);

        Jwt accessJwt = jwtService.generateJwt(
                jwtConfig, String.valueOf(user.getId()),
                JwtType.ACCESS, JwtProfileType.USER,
                jwtConfig.getExpires().publishAccessTokenExpiry(), null);

        Jwt refreshJwt = jwtService.generateJwt(
                jwtConfig, String.valueOf(user.getId()),
                JwtType.REFRESH, JwtProfileType.USER,
                jwtConfig.getExpires().publishRefreshTokenExpiry(), null);

        String userName = URLEncoder.encode(user.getName(), StandardCharsets.UTF_8);
        String redirectUrl = frontendUrl + "/auth/callback"
                + "?accessToken=" + accessJwt.getToken()
                + "&refreshToken=" + refreshJwt.getToken()
                + "&userId=" + user.getId()
                + "&name=" + userName
                + "&email=" + URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8);

        log.info("OAuth 로그인 성공: {} ({})", user.getEmail(), user.getProvider());
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
