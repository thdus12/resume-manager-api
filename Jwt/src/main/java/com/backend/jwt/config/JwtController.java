package com.backend.jwt.config;

import com.backend.jwt.type.JwtProfileType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Qualifier
@RestController
public @interface JwtController {
    /**
     * JwtProfileType
     *
     * @return Jwt 프로필 명
     */
    JwtProfileType[] value();
}