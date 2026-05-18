package com.devksg.withcoworker.config;

import com.devksg.withcoworker.domain.ProviderType;
import com.devksg.withcoworker.domain.User;
import com.devksg.withcoworker.repository.AuthProviderRepository;
import com.devksg.withcoworker.repository.TeamMemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${app.frontend-url:}")
    private String frontendUrl;

    private final AuthProviderRepository authProviderRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String googleId = oAuth2User.getAttribute("sub");

        var authProvider = authProviderRepository.findByProviderAndProviderId(ProviderType.GOOGLE, googleId).orElseThrow();

        User user = authProvider.getUser();
        String token = jwtTokenProvider.generateToken(user.getId());
        boolean isExistingMember = teamMemberRepository.existsByUserId(user.getId());
        String redirectPath = isExistingMember ? "/dashboard" : "/team-select";

        response.sendRedirect(frontendUrl + redirectPath + "?token=" + token);
    }
}
