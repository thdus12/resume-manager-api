package com.resumemanager.domain.resume.model.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ResumeUpdateRequest {
    private String title;
    private String templateId;
    private Map<String, Object> style;
    private List<Map<String, Object>> blocks;
}
