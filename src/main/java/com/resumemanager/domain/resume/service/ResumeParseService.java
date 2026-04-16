package com.resumemanager.domain.resume.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumemanager.core.error.entity.ApiException;
import com.resumemanager.core.error.type.ErrorCode;
import com.resumemanager.domain.llm.service.LLMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeParseService {

    private final LLMService llmService;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
            당신은 이력서 파싱 전문가입니다. 주어진 텍스트에서 정보를 추출하여 아래 JSON 구조로 변환하세요.

            반드시 아래 형식의 JSON만 반환하세요. 다른 텍스트는 포함하지 마세요.

            {
              "blocks": [
                {
                  "type": "profile",
                  "title": "기본 정보",
                  "data": { "name": "", "email": "", "phone": "", "address": "", "photo": "" }
                },
                {
                  "type": "education",
                  "title": "학력",
                  "data": {
                    "items": [
                      { "id": "uuid", "school": "", "major": "", "degree": "", "startDate": "YYYY-MM", "endDate": "YYYY-MM" }
                    ]
                  }
                },
                {
                  "type": "experience",
                  "title": "경력",
                  "data": {
                    "items": [
                      { "id": "uuid", "company": "", "position": "", "startDate": "YYYY-MM", "endDate": "YYYY-MM", "techStack": "", "description": "" }
                    ]
                  }
                },
                {
                  "type": "project",
                  "title": "프로젝트",
                  "data": {
                    "items": [
                      { "id": "uuid", "name": "", "role": "", "startDate": "YYYY-MM", "endDate": "YYYY-MM", "techStack": "", "description": "" }
                    ]
                  }
                },
                {
                  "type": "skill",
                  "title": "기술 스택",
                  "data": {
                    "items": [
                      { "id": "uuid", "category": "", "skills": "" }
                    ]
                  }
                },
                {
                  "type": "certificate",
                  "title": "자격증/어학",
                  "data": {
                    "items": [
                      { "id": "uuid", "name": "", "issuer": "", "date": "YYYY-MM" }
                    ]
                  }
                },
                {
                  "type": "introduction",
                  "title": "자기소개",
                  "data": { "content": "" }
                }
              ]
            }

            규칙:
            - 이력서에 해당 정보가 없으면 해당 블록은 포함하지 마세요.
            - 날짜는 YYYY-MM 형식으로 변환하세요.
            - 각 item의 id는 고유한 임의 문자열로 생성하세요.
            - JSON만 반환하세요.""";

    public Map<String, Object> parseResume(MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            String ext = filename != null ? filename.substring(filename.lastIndexOf('.')).toLowerCase() : "";
            String text = extractText(file, ext);

            if (text == null || text.isBlank()) {
                throw new ApiException(ErrorCode.BAD_REQUEST, "파일에서 텍스트를 추출할 수 없습니다");
            }

            String userPrompt = "다음 이력서 텍스트를 분석하여 JSON 블록 구조로 변환해주세요:\n\n" + text;
            String response = llmService.query(SYSTEM_PROMPT, userPrompt);
            String json = extractJson(response);
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("이력서 파싱 실패", e);
            throw new ApiException(ErrorCode.LLM_ERROR, "이력서 파싱 중 오류: " + e.getMessage());
        }
    }

    private String extractText(MultipartFile file, String ext) throws Exception {
        if (".pdf".equals(ext)) {
            try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
                return new PDFTextStripper().getText(doc);
            }
        }
        if (".docx".equals(ext) || ".doc".equals(ext)) {
            try (InputStream is = file.getInputStream();
                 XWPFDocument doc = new XWPFDocument(is)) {
                StringBuilder sb = new StringBuilder();
                for (XWPFParagraph p : doc.getParagraphs()) {
                    sb.append(p.getText()).append("\n");
                }
                return sb.toString();
            }
        }
        // txt 등 텍스트 파일
        return new String(file.getBytes(), "UTF-8");
    }

    private String extractJson(String text) {
        Pattern pattern = Pattern.compile("```json?\\s*([\\s\\S]*?)```");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return text.trim();
    }
}
