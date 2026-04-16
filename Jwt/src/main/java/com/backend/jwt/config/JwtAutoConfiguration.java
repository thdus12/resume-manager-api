package com.backend.jwt.config;

import com.backend.jwt.crypto.ChaCha20;
import com.backend.jwt.error.exception.YamlPropertyValidationException;
import com.backend.jwt.model.JwtProfiles;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@AutoConfigureAfter(EnvironmentPostProcessor.class)
@RequiredArgsConstructor
@EnableAspectJAutoProxy
@ComponentScan(basePackages = {"com.backend"})
public class JwtAutoConfiguration implements ServletContextInitializer {
    private final Environment environment;
    private final Validator validator;
    private JwtProfiles jwtProfiles;
    private ChaCha20 chaCha20;

    /**
     * 애플리케이션 시작 시 설정 유효성 검사 수행
     */
    @PostConstruct
    public void validateConfigurations() {
        this.chaCha20 = validateConfig("chacha20", ChaCha20.class);
        this.jwtProfiles = validateConfig("jwt", JwtProfiles.class);
    }

    /**
     * 설정을 로드하고 유효성을 검사함
     *
     * @param yamlName 설정의 YAML 접두사
     * @param clazz 설정 클래스 타입
     * @return 검증된 설정 객체
     * @throws YamlPropertyValidationException 설정이 없거나 유효성 검사에 실패한 경우
     */
    private <T> T validateConfig(String yamlName, Class<T> clazz) {
        Binder binder = Binder.get(environment);
        BindResult<T> bindResult = binder.bind(yamlName, clazz);

        if (!bindResult.isBound()) {
            throw new YamlPropertyValidationException(yamlName + " 설정이 없습니다. application.yml 파일에 " + yamlName + " 설정을 추가해주세요.");
        }

        T config = bindResult.get();
        Set<ConstraintViolation<T>> violations = validator.validate(config);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                .map(v -> v.getPropertyPath() + v.getMessage())
                .collect(Collectors.joining(", "));
            throw new YamlPropertyValidationException(yamlName + " 설정 유효성 검사 실패: " + errorMessage);
        }

        log.info("{} configuration validated successfully", yamlName);
        return config;
    }

    @Bean
    public ChaCha20 chaCha20() {
        return this.chaCha20; // 검증된 ChaCha20 설정 빈 제공
    }

    @Bean
    public JwtProfiles jwtProfiles() {
        return this.jwtProfiles; // 검증된 JwtProfiles 설정 빈 제공
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        // 서블릿 컨텍스트 초기화 로직 (필요한 경우)
    }
}