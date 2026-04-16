package com.backend.jwt.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.Map;

@Slf4j
@Component
public class YamlValidator implements ConstraintValidator<YamlValidation, Object> {

    @Override
    public void initialize(YamlValidation constraintAnnotation) {
        // 초기화가 필요 없으므로 비워둡니다.
    }

    /**
     * 객체의 유효성 검사
     *
     * @param obj 검사할 객체
     * @param context 제약 조건 위반 컨텍스트
     * @return 유효성 검사 통과 여부
     */
    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        if (obj == null) {
            return true;
        }

        StringBuilder errorBuilder = new StringBuilder();
        validateObject(obj, "", errorBuilder);

        if (errorBuilder.isEmpty()) {
            return true;
        }

        // 오류가 있는 경우 기본 제약조건 비활성화 후 커스텀 메시지 추가
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(errorBuilder.toString().trim())
            .addConstraintViolation();
        return false;
    }

    /**
     * 객체의 모든 필드를 재귀적으로 검증
     *
     * @param obj 검사할 객체
     * @param path 현재 객체의 경로
     * @param errorBuilder 오류 메시지를 저장할 StringBuilder
     */
    private void validateObject(Object obj, String path, StringBuilder errorBuilder) {
        if (obj == null) {
            return;
        }

        Class<?> clazz = obj.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            validateField(obj, field, path, errorBuilder);
        }
    }

    /**
     * 개별 필드 검증
     *
     * @param obj 필드가 속한 객체
     * @param field 검사할 필드
     * @param path 현재 필드의 경로
     * @param errorBuilder 오류 메시지를 저장할 StringBuilder
     */
    private void validateField(Object obj, Field field, String path, StringBuilder errorBuilder) {
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(obj.getClass(), MethodHandles.lookup());
            VarHandle varHandle = lookup.findVarHandle(obj.getClass(), field.getName(), field.getType());
            Object value = varHandle.get(obj);
            String currentPath = buildCurrentPath(path, field.getName());

            if (value == null) {
                appendError(errorBuilder, currentPath, "설정이 존재하지 않습니다.");
            } else if (isEmptyString(String.valueOf(value))) {
                appendError(errorBuilder, currentPath, "값이 비었습니다.");
            } else if (value instanceof Map) {
                validateMap((Map<?, ?>) value, currentPath, errorBuilder);
            } else if (value.getClass().isAnnotationPresent(YamlValidation.class)) {
                validateObject(value, currentPath, errorBuilder);
            }
        } catch (IllegalAccessException | RuntimeException | NoSuchFieldException e) {
            log.error("필드 접근 중 오류 발생: {}", e.getMessage());
            appendError(errorBuilder, path, "필드 접근 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * Map 타입 필드의 모든 엔트리 검증
     *
     * @param map 검사할 Map 객체
     * @param path 현재 Map의 경로
     * @param errorBuilder 오류 메시지를 저장할 StringBuilder
     */
    private void validateMap(Map<?, ?> map, String path, StringBuilder errorBuilder) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            validateObject(entry.getValue(), path + " -> " + entry.getKey(), errorBuilder);
        }
    }

    /**
     * 현재 필드의 전체 경로 생성
     *
     * @param path 상위 경로
     * @param fieldName 현재 필드 이름
     * @return 생성된 전체 경로
     */
    private String buildCurrentPath(String path, String fieldName) {
        return path.isEmpty() ? fieldName : path + " -> " + fieldName;
    }

    /**
     * 빈 문자열인지 확인
     *
     * @param value 검사할 객체
     * @return 비어있는 문자열인지 여부
     */
    private boolean isEmptyString(String value) {
        return value.trim().isEmpty();
    }

    /**
     * errorBuilder 에 오류 메시지 추가
     *
     * @param errorBuilder 오류 메시지를 저장할 StringBuilder
     * @param path 오류가 발생한 필드의 경로
     * @param message 오류 메시지
     */
    private void appendError(StringBuilder errorBuilder, String path, String message) {
        errorBuilder.append(path).append(" ").append(message).append(" ");
    }
}