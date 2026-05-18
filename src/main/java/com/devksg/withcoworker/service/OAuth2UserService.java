package com.devksg.withcoworker.service;

import com.devksg.withcoworker.domain.AuthProvider;
import com.devksg.withcoworker.domain.ProviderType;
import com.devksg.withcoworker.domain.User;
import com.devksg.withcoworker.repository.AuthProviderRepository;
import com.devksg.withcoworker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final AuthProviderRepository authProviderRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String googleId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        authProviderRepository.findByProviderAndProviderId(ProviderType.GOOGLE, googleId)
            .orElseGet(() -> {
                User user = userRepository.findByEmail(email)
                    .orElseGet(() -> userRepository.save(User.builder()
                        .email(email)
                        .name(name)
                        .build()));
                return authProviderRepository.save(AuthProvider.builder()
                    .user(user)
                    .provider(ProviderType.GOOGLE)
                    .providerId(googleId)
                    .build());
            });

        return oAuth2User;
    }
}
