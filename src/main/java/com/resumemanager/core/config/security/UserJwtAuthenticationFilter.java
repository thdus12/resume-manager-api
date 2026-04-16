package com.resumemanager.core.config.security;

import com.backend.jwt.config.JwtProfilePathFinder;
import com.backend.jwt.model.Jwt;
import com.backend.jwt.model.JwtConfig;
import com.backend.jwt.security.JwtAuthenticationFilter;
import com.backend.jwt.service.AccessTokenService;
import com.backend.jwt.service.JwtCookieService;
import com.backend.jwt.service.JwtService;
import com.backend.jwt.type.JwtProfileType;
import com.resumemanager.domain.user.entity.User;
import com.resumemanager.domain.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class UserJwtAuthenticationFilter extends JwtAuthenticationFilter<UserDetails> {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final JwtProfilePathFinder jwtProfilePathFinder;
    private final AccessTokenService accessTokenService;

    public UserJwtAuthenticationFilter(
            JwtCookieService cookieService,
            JwtProfilePathFinder jwtProfilePathFinder,
            JwtService jwtService,
            AccessTokenService accessTokenService,
            UserRepository userRepository) {
        super(cookieService, jwtProfilePathFinder, jwtService, accessTokenService);
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.jwtProfilePathFinder = jwtProfilePathFinder;
        this.accessTokenService = accessTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        JwtConfig jwtConfig = jwtProfilePathFinder.getJwtConfig(JwtProfileType.USER);
        String accessToken;

        try {
            accessToken = accessTokenService.extract(request, response);
        } catch (Exception e) {
            log.warn("토큰 추출 실패: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if (accessToken == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Jwt jwt;
        try {
            jwt = jwtService.parseJwt(jwtConfig, accessToken);
        } catch (Exception e) {
            log.warn("JWT 파싱 실패: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if (jwt.validate() || jwt.isExpired()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            UserDetails principal = createPrincipal(jwt);
            if (principal == null || !principal.isEnabled()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            Authentication authentication = new UsernamePasswordAuthenticationToken(principal, jwt, principal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            log.error("인증 처리 실패: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        if (path.equals("/auth/me")) return false;
        if (path.startsWith("/auth/")) return true;
        if (path.startsWith("/oauth2/") || path.startsWith("/login/oauth2/")) return true;
        return false;
    }

    @Override
    protected UserDetails createPrincipal(Jwt jwt) {
        String encryptedSubject = jwt.getSubject();
        String userIdStr = jwtService.decryptSubject(encryptedSubject, jwt.getCreatedAt());
        Long userId = Long.parseLong(userIdStr);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
        return new UserPrincipal(user);
    }
}
