package com.resumemanager.domain.auth.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenRefreshRequest {
    @NotBlank(message = "refreshToken은 필수입니다")
    private String refreshToken;
}
