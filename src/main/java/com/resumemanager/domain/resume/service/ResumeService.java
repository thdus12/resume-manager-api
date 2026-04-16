package com.resumemanager.domain.resume.service;

import com.resumemanager.core.error.entity.ApiException;
import com.resumemanager.core.error.type.ErrorCode;
import com.resumemanager.domain.resume.entity.Resume;
import com.resumemanager.domain.resume.model.dto.request.ResumeCreateRequest;
import com.resumemanager.domain.resume.model.dto.request.ResumeUpdateRequest;
import com.resumemanager.domain.resume.model.dto.response.ResumeResponse;
import com.resumemanager.domain.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResumeService {

    private final ResumeRepository resumeRepository;

    public List<ResumeResponse> getResumes(Long userId) {
        return resumeRepository.findByUserIdOrderByLastModifiedDateDesc(userId)
                .stream().map(ResumeResponse::from).toList();
    }

    public ResumeResponse getResume(Long id, Long userId) {
        Resume resume = resumeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESUME_NOT_FOUND));
        return ResumeResponse.from(resume);
    }

    @Transactional
    public ResumeResponse createResume(Long userId, ResumeCreateRequest request) {
        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setTitle(request.getTitle());
        resume.setTemplateId(request.getTemplateId() != null ? request.getTemplateId() : "classic");
        resume.setStyle(request.getStyle() != null ? request.getStyle() : new HashMap<>());
        resume.setBlocks(new ArrayList<>());
        return ResumeResponse.from(resumeRepository.save(resume));
    }

    @Transactional
    public ResumeResponse updateResume(Long id, Long userId, ResumeUpdateRequest request) {
        Resume resume = resumeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESUME_NOT_FOUND));
        if (request.getTitle() != null) resume.setTitle(request.getTitle());
        if (request.getTemplateId() != null) resume.setTemplateId(request.getTemplateId());
        if (request.getStyle() != null) resume.setStyle(request.getStyle());
        if (request.getBlocks() != null) resume.setBlocks(request.getBlocks());
        return ResumeResponse.from(resumeRepository.save(resume));
    }

    @Transactional
    public void deleteResume(Long id, Long userId) {
        if (resumeRepository.findByIdAndUserId(id, userId).isEmpty()) {
            throw new ApiException(ErrorCode.RESUME_NOT_FOUND);
        }
        resumeRepository.deleteByIdAndUserId(id, userId);
    }
}
