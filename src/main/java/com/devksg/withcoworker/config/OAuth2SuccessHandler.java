package com.devksg.withcoworker.config;

import com.devksg.withcoworker.repository.TeamMemberRepository;
import com.devksg.withcoworker.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${app.frontend-url:}")
    private String frontendUrl;

    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String googleId = oAuth2User.getAttribute("sub");

        boolean isExistingMember = userRepository.findByGoogleId(googleId)
                .map(user -> teamMemberRepository.existsByUserId(user.getId()))
                .orElse(false);

        String redirectUrl = isExistingMember ? "/dashboard" : "/team-select";
        response.sendRedirect(frontendUrl + redirectUrl);
    }
}
