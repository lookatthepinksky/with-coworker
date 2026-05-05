package com.devksg.withcoworker.service;

import com.devksg.withcoworker.domain.User;
import com.devksg.withcoworker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String googleId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        userRepository.findByGoogleId(googleId).orElseGet(() ->
            userRepository.save(
                User.builder()
                    .googleId(googleId)
                    .email(email)
                    .name(name)
                    .build()
            )
        );

        return oAuth2User;
    }
}
