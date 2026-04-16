package com.backend.jwt.config;

import com.backend.jwt.type.JwtProfileType;
import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Qualifier
public @interface JwtProfile {
    /**
     * JwtProfileType
     *
     * @return Jwt 프로필 명
     */
    JwtProfileType value();
}