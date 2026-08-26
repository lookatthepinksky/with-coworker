package com.devksg.withcoworkers.config;

import com.devksg.withcoworkers.domain.ProviderType;
import com.devksg.withcoworkers.domain.User;
import com.devksg.withcoworkers.repository.AuthProviderRepository;
import com.devksg.withcoworkers.repository.TeamMemberRepository;
import com.devksg.withcoworkers.service.UserSessionService;
import com.devksg.withcoworkers.service.UserTeamCacheService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
    private final UserSessionService userSessionService;
    private final UserTeamCacheService userTeamCacheService;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String googleId = oAuth2User.getAttribute("sub");

        var authProvider = authProviderRepository.findByProviderAndProviderId(ProviderType.GOOGLE, googleId).orElseThrow();

        User user = authProvider.getUser();
        String token = jwtTokenProvider.generateToken(user.getId());
        userSessionService.save(user.getId(), token);

        ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .path("/")
                .maxAge(jwtTokenProvider.getExpirationSeconds())
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        var approvedMember = teamMemberRepository.findByUserId(user.getId());
        boolean isApproved = approvedMember.isPresent();

        Long teamId = isApproved ? approvedMember.get().getTeam().getId() : null;
        String teamName = isApproved ? approvedMember.get().getTeam().getName() : null;
        boolean isAdmin = isApproved && approvedMember.get().getRole() == com.devksg.withcoworkers.domain.TeamMemberRole.ADMIN;
        userTeamCacheService.saveUserInfo(user.getId(), user.getName(), user.getEmail(), teamId, teamName, isAdmin);

        boolean hasPending = !isApproved && teamMemberRepository.existsByUserId(user.getId());

        String redirectPath;
        if (isApproved) {
            redirectPath = "/team-members/overview";
        } else if (hasPending) {
            redirectPath = "/team-select?pending=true";
        } else {
            redirectPath = "/team-select";
        }

        response.sendRedirect(frontendUrl + redirectPath);
    }
}
