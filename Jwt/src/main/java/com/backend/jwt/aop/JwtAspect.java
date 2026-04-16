package com.backend.jwt.aop;

import com.backend.jwt.config.JwtProfilePathFinder;
import com.backend.jwt.model.JwtConfig;
import com.backend.jwt.type.JwtProfileType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * JWT 관련 설정을 컨트롤러 메서드에 자동으로 주입하는 Aspect
 */
@Aspect
@Component
@RequiredArgsConstructor
public class JwtAspect {
    private final JwtProfilePathFinder jwtProfilePathFinder;

    /**
     * 특정 패키지 내의 Controller 클래스 중 @JwtController 어노테이션이 붙은 클래스의 모든 메서드를 대상으로 하는 Pointcut 정의
     * 이 Pointcut은 다음 두 가지 조건을 모두 만족해야 합니다:
     * 1. com.shinhanfriends.controller 패키지 및 그 하위 패키지에 있는 클래스 중 이름이 Controller로 끝나는 클래스의 모든 메서드
     * 2. @JwtController 어노테이션이 붙은 클래스
     */
    @Pointcut("execution(* com.petfortune.controller..*Controller.*(..)) && @within(com.backend.jwt.config.JwtController)")
    public void jwtController() {}

    /**
     * JWT 설정을 주입하는 Around 어드바이스
     * 대상 메서드 실행 전후에 JWT 관련 설정을 주입합니다.
     *
     * @param joinPoint 현재 실행 중인 메서드에 대한 정보를 담고 있는 객체
     * @return 대상 메서드의 실행 결과
     * @throws Throwable 메서드 실행 중 발생할 수 있는 모든 예외
     */
    @Around("jwtController()")
    public Object injectJwtConfig(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("Request attributes are not available");
        }

        HttpServletRequest request = attributes.getRequest();
        String path = request.getRequestURI();

        JwtProfileType profileType = jwtProfilePathFinder.findProfileType(path);
        JwtConfig jwtConfig = jwtProfilePathFinder.getJwtConfig(profileType);

        Object[] args = joinPoint.getArgs();
        Class<?>[] parameterTypes = method.getParameterTypes();

        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i] == JwtConfig.class) {
                args[i] = jwtConfig;
            } else if (parameterTypes[i] == JwtProfileType.class) {
                args[i] = profileType;
            }
        }

        return joinPoint.proceed(args);
    }
}