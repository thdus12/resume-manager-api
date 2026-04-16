package com.resumemanager.domain.resume.controller;

import com.resumemanager.domain.auth.service.AuthService;
import com.resumemanager.domain.resume.model.dto.request.ResumeCreateRequest;
import com.resumemanager.domain.resume.model.dto.request.ResumeUpdateRequest;
import com.resumemanager.domain.resume.model.dto.response.ResumeResponse;
import com.resumemanager.domain.resume.service.ResumeService;
import com.resumemanager.domain.resume.service.ResumeParseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;
    private final ResumeParseService resumeParseService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<List<ResumeResponse>> list() {
        Long userId = authService.getCurrentUserId();
        return ResponseEntity.ok(resumeService.getResumes(userId));
    }

    @PostMapping
    public ResponseEntity<ResumeResponse> create(@Valid @RequestBody ResumeCreateRequest request) {
        Long userId = authService.getCurrentUserId();
        return ResponseEntity.ok(resumeService.createResume(userId, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeResponse> get(@PathVariable Long id) {
        Long userId = authService.getCurrentUserId();
        return ResponseEntity.ok(resumeService.getResume(id, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResumeResponse> update(@PathVariable Long id, @RequestBody ResumeUpdateRequest request) {
        Long userId = authService.getCurrentUserId();
        return ResponseEntity.ok(resumeService.updateResume(id, userId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long userId = authService.getCurrentUserId();
        resumeService.deleteResume(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/parse")
    public ResponseEntity<Map<String, Object>> parse(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = resumeParseService.parseResume(file);
        return ResponseEntity.ok(result);
    }
}
