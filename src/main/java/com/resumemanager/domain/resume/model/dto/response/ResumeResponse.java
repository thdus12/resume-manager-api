package com.resumemanager.domain.resume.model.dto.response;

import com.resumemanager.domain.resume.entity.Resume;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class ResumeResponse {
    private Long id;
    private String title;
    private String templateId;
    private Map<String, Object> style;
    private List<Map<String, Object>> blocks;
    private Instant createdAt;
    private Instant updatedAt;

    public static ResumeResponse from(Resume resume) {
        return new ResumeResponse(
                resume.getId(),
                resume.getTitle(),
                resume.getTemplateId(),
                resume.getStyle(),
                resume.getBlocks(),
                resume.getCreatedDate(),
                resume.getLastModifiedDate());
    }
}
