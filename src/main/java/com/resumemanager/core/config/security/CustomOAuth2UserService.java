package com.resumemanager.core.config.security;

import com.resumemanager.domain.user.entity.User;
import com.resumemanager.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        String providerId = extractProviderId(registrationId, oAuth2User);
        String email = extractEmail(registrationId, oAuth2User);
        String name = extractName(registrationId, oAuth2User);

        User user = userRepository.findByProviderAndProviderId(registrationId, providerId)
                .orElseGet(() -> {
                    // 같은 이메일로 다른 provider 가입 여부 확인
                    User existing = userRepository.findByEmail(email).orElse(null);
                    if (existing != null) {
                        // 기존 이메일 유저에 OAuth 연결
                        existing.setProvider(registrationId);
                        existing.setProviderId(providerId);
                        return userRepository.save(existing);
                    }
                    // 신규 유저 생성
                    User newUser = new User(name, email, registrationId, providerId);
                    log.info("OAuth 회원가입: {} ({})", email, registrationId);
                    return userRepository.save(newUser);
                });

        // 이름 업데이트
        if (name != null && !name.equals(user.getName())) {
            user.setName(name);
            userRepository.save(user);
        }

        return new OAuth2UserPrincipal(user, oAuth2User.getAttributes());
    }

    private String extractProviderId(String provider, OAuth2User oAuth2User) {
        return switch (provider) {
            case "google" -> oAuth2User.getAttribute("sub");
            case "kakao" -> String.valueOf(oAuth2User.getAttribute("id"));
            case "naver" -> {
                Map<String, Object> response = oAuth2User.getAttribute("response");
                yield response != null ? (String) response.get("id") : null;
            }
            default -> oAuth2User.getName();
        };
    }

    private String extractEmail(String provider, OAuth2User oAuth2User) {
        return switch (provider) {
            case "google" -> oAuth2User.getAttribute("email");
            case "kakao" -> {
                Map<String, Object> account = oAuth2User.getAttribute("kakao_account");
                yield account != null ? (String) account.get("email") : null;
            }
            case "naver" -> {
                Map<String, Object> response = oAuth2User.getAttribute("response");
                yield response != null ? (String) response.get("email") : null;
            }
            default -> oAuth2User.getAttribute("email");
        };
    }

    private String extractName(String provider, OAuth2User oAuth2User) {
        return switch (provider) {
            case "google" -> oAuth2User.getAttribute("name");
            case "kakao" -> {
                Map<String, Object> properties = oAuth2User.getAttribute("properties");
                yield properties != null ? (String) properties.get("nickname") : null;
            }
            case "naver" -> {
                Map<String, Object> response = oAuth2User.getAttribute("response");
                yield response != null ? (String) response.get("name") : null;
            }
            default -> oAuth2User.getAttribute("name");
        };
    }
}
