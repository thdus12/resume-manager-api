package com.backend.jwt.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.backend.jwt.config.JwtProfilePathFinder;
import com.backend.jwt.error.entity.ErrorResponse;
import com.backend.jwt.error.entity.JwtErrorCode;
import com.backend.jwt.error.exception.JwtException;
import com.backend.jwt.model.Jwt;
import com.backend.jwt.model.JwtConfig;
import com.backend.jwt.service.AccessTokenService;
import com.backend.jwt.service.JwtCookieService;
import com.backend.jwt.service.JwtService;
import com.backend.jwt.type.JwtProfileType;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.OutputStream;

@Slf4j
@AllArgsConstructor
@Order(1)
public abstract class JwtAuthenticationFilter<T extends UserDetails> extends OncePerRequestFilter {
    private final JwtCookieService cookieService;
    private final JwtProfilePathFinder jwtProfilePathFinder;
    private final JwtService jwtService;
    private final AccessTokenService accessTokenService;

    /**
     * JWT 토큰의 유효성을 검사하고, 인증 정보를 SecurityContextHolder 에 설정
     *
     * @param request  현재 HTTP 요청 객체
     * @param response 현재 HTTP 응답 객체
     * @param filterChain 다음 필터 체인
     * @throws ServletException 서블릿 예외
     * @throws IOException I/O 예외
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        // 필터링하지 않을 요청인지 확인
        if (shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        JwtProfileType jwtProfileType = jwtProfilePathFinder.findProfileType(request.getRequestURI());
        if (jwtProfileType == null) {
            filterChain.doFilter(request, response);
            return;
        }

        JwtConfig jwtConfig = jwtProfilePathFinder.getJwtConfig(jwtProfileType);
        String accessToken = accessTokenService.extract(request, response);

        // Access Token 검증
        Jwt jwt;
        try {
            jwt = parseJwt(jwtConfig, accessToken);
        } catch (JwtException | io.jsonwebtoken.JwtException e) {
            handleInvalidToken(response, jwtConfig);
            return;
        }

        if (jwt.validate()) {
            handleInvalidToken(response, jwtConfig);
            return;
        }

        if (jwt.isExpired()) {
            setErrorResponse(response, JwtErrorCode.JWT_UNAUTHORIZED, "만료된 토큰 입니다.");
            return;
        }

        try {
            T principal = createPrincipal(jwt);
            if (principal == null) {
                return;
            }

            if (!principal.isEnabled()) {
                handleDisabledUser(response, jwtConfig);
                return;
            }

            Authentication authentication = new UsernamePasswordAuthenticationToken(principal, jwt, principal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (JwtException e) {
            setErrorResponse(response, e.getErrorCode(), e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 이 필터가 적용되지 않아야 할 요청인지 확인하는 메서드
     * 필요에 따라 자식 클래스에서 구현
     *
     * @param request 현재 HTTP 요청 객체
     * @return 필터가 적용되지 않아야 하면 true, 그렇지 않으면 false를 반환
     * @throws ServletException 서블릿 예외
     */
    @Override
    protected abstract boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException;

    /**
     * JWT 토큰을 파싱하는 메서드
     * 필요에 따라 자식 클래스에서 구현
     *
     * @param jwtConfig JWT 구성 정보
     * @param token JWT 토큰 문자열
     * @return 파싱된 JWT 객체
     */
    protected Jwt parseJwt(JwtConfig jwtConfig, String token) {
        return jwtService.parseJwt(jwtConfig, token);
    }

    /**
     * JWT 토큰에서 사용자 정보를 생성하는 메서드
     * 필요에 따라 자식 클래스에서 구현
     *
     * @param jwt JWT 객체
     * @return 사용자 정보를 담고 있는 UserDetails 구현체 객체
     */
    protected abstract T createPrincipal(Jwt jwt);

    /**
     * 유효하지 않은 JWT 토큰 처리 메서드
     * 토큰을 삭제하고 오류 응답 전송
     *
     * @param response HTTP 응답 객체
     * @param jwtConfig JWT 구성 정보
     * @throws IOException I/O 예외
     */
    private void handleInvalidToken(HttpServletResponse response, JwtConfig jwtConfig) throws IOException {
        cookieService.removeCookie(response, jwtConfig.getNames().getAccessToken());
        cookieService.removeCookie(response, jwtConfig.getNames().getRefreshToken());
        setErrorResponse(response, JwtErrorCode.JWT_TOKEN_INVALID, "유효하지 않은 토큰 입니다.");
    }

    /**
     * 비활성화된 사용자 처리 메서드
     * 토큰을 삭제하고 오류 응답 전송
     *
     * @param response HTTP 응답 객체
     * @param jwtConfig JWT 구성 정보
     * @throws IOException I/O 예외
     */
    private void handleDisabledUser(HttpServletResponse response, JwtConfig jwtConfig) throws IOException {
        cookieService.removeCookie(response, jwtConfig.getNames().getAccessToken());
        cookieService.removeCookie(response, jwtConfig.getNames().getRefreshToken());
        setErrorResponse(response, JwtErrorCode.JWT_USER_DISABLED, "비활성화된 사용자 입니다.");
    }

    /**
     * 오류 응답 전송
     *
     * @param response HTTP 응답 객체
     * @param errorCode 오류 코드
     * @throws IOException I/O 예외
     */
    private void setErrorResponse(HttpServletResponse response, JwtErrorCode errorCode, String body) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(errorCode.getStatus().value());
        OutputStream responseStream = response.getOutputStream();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(responseStream, ErrorResponse.status(errorCode).body(body).getBody());
        responseStream.flush();
    }
}
