package com.resumemanager.domain.resume.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ResumeCreateRequest {
    @NotBlank(message = "제목은 필수입니다")
    private String title;
    private String templateId = "classic";
    private Map<String, Object> style;
}
