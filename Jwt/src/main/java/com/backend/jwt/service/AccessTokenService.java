package com.backend.jwt.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.backend.jwt.error.entity.ErrorResponse;
import com.backend.jwt.error.entity.JwtErrorCode;
import com.backend.jwt.error.exception.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessTokenService {

    public String extract(String token) {
        if (token == null) {
            throw new JwtException(JwtErrorCode.JWT_VALIDATE_ERROR, "헤더 Authorization 정보가 잘못되었습니다.");
        }

        if (token.contains("Bearer ")) {
            token = token.replace("Bearer ", "");
        }
        return token;
    }

    /**
     * `Bearer {AccessToken}` 형태의 문자열을 {AccessToken}으로 변경하는 메소드입니다.
     *
     * @param response HTTP 응답
     * @param token    변경할 문자열
     * @return token
     */
    public String extract(String token, HttpServletResponse response) throws IOException {

        if (token == null) {
            setErrorResponse(response, JwtErrorCode.JWT_VALIDATE_ERROR, "헤더 Authorization 정보가 잘못되었습니다.");
        } else {
            if (!token.startsWith("Bearer ")) {
                setErrorResponse(response, JwtErrorCode.JWT_VALIDATE_ERROR, "헤더 Authorization 정보가 잘못되었습니다.");
            }
        }
        return extract(token);
    }

    /**
     * HttpServletRequest의 Header에서 토큰을 추출하여, {AccessToken}만 반환합니다.
     *
     * @param request  HTTP 요청
     * @param response HTTP 응답
     * @return token
     */
    public String extract(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return extract(request.getHeader("Authorization"), response);
    }

    /**
     * 오류 응답 전송
     *
     * @param response  HTTP 응답 객체
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
