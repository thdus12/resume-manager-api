package com.backend.jwt.type;


import lombok.Getter;

@Getter
public enum JwtType {
    ACCESS("액세스 토큰"),
    REFRESH("리프레시 토큰"),
    PASSWORD_STALE("비밀번호 갱신용 토큰"),
    PASSWORD_CHANGE("비밀번호 찾기 후 변경용 토큰"),
    SIGN_IN_RE("인증 번호 로그인용 토큰"),
    ;

    private final String desc;

    JwtType(String desc) {
        this.desc = desc;
    }

    public static JwtType fromName(String text) {
        for (JwtType value : JwtType.values()) {
            if (value.name().equalsIgnoreCase(text)) {
                return value;
            }
        }
        return null;
    }
}
