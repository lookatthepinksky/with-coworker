package com.devksg.withcoworker.controller;

import com.devksg.withcoworker.domain.User;
import com.devksg.withcoworker.dto.DashboardResponse;
import com.devksg.withcoworker.repository.UserRepository;
import com.devksg.withcoworker.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(@AuthenticationPrincipal OAuth2User oAuth2User) {
        String googleId = oAuth2User.getAttribute("sub"); // 고유 ID (숫자로된 긴 문자열)
        User me = userRepository.findByGoogleId(googleId).orElseThrow();
        return ResponseEntity.ok(dashboardService.getDashboard(me));
    }
}
