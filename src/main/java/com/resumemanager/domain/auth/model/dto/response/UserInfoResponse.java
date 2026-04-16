package com.resumemanager.domain.auth.model.dto.response;

import com.resumemanager.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoResponse {
    private Long id;
    private String email;
    private String name;

    public static UserInfoResponse from(User user) {
        return new UserInfoResponse(user.getId(), user.getEmail(), user.getName());
    }
}
