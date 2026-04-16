package com.backend.jwt.config;

import com.backend.jwt.model.JwtConfig;
import com.backend.jwt.model.JwtProfiles;
import com.backend.jwt.type.JwtProfileType;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProfilePathFinder implements ApplicationListener<ContextRefreshedEvent> {
    private final JwtProfiles profiles;
    private final Map<String, JwtProfileType> pathToProfileTypeMap = new HashMap<>();

    @PostConstruct
    public void init() {
        log.info("Initializing JwtProfilePathFinder");

    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();
        // @JwtController 어노테이션이 붙은 모든 빈 추출
        Map<String, Object> beans = context.getBeansWithAnnotation(JwtController.class);

        for (Object bean : beans.values()) {
            // 어떤 JDK를 써도 대응할 수 있도록 아래 코드 사용해야함 (필수)
            Class<?> controllerClass = AopProxyUtils.ultimateTargetClass(bean);

            JwtController jwtController = controllerClass.getAnnotation(JwtController.class);
            RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(controllerClass, RequestMapping.class);
            if (requestMapping != null && requestMapping.value().length > 0) {
                String path = requestMapping.value()[0];
                JwtProfileType profileType = jwtController.value()[0];
                pathToProfileTypeMap.put(path, profileType);
            }
        }
    }

    /**
     * JWT 프로필에 맞는 JwtConfig 반환
     *
     * @param profileType jwt 프로필 타입
     * @return JWT 프로필에 맞는 JwtConfig 반환. 없으면 null 반환
     */
    public JwtConfig getJwtConfig(JwtProfileType profileType) {
        if (profileType == null) {
            throw new IllegalStateException("JWT profile type is null");
        }
        JwtConfig profile = this.profiles.getProfile(profileType.getDesc());
        if (profile == null) {
            throw new IllegalStateException("JWT profile not found");
        }
        return profile;
    }

    /**
     * JWT 프로필 타입 반환
     *
     * @param path 요청 경로
     * @return JWT 프로필 타입 반환. 없으면 null 반환
     */
    @Nullable
    public JwtProfileType findProfileType(String path) {
        return pathToProfileTypeMap.entrySet().stream()
            .filter(entry -> path.startsWith(entry.getKey()))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(null);
    }
}
