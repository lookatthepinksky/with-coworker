package com.devksg.withcoworker.controller;

import com.devksg.withcoworker.domain.User;
import com.devksg.withcoworker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal OAuth2User oAuth2User) {
        if (oAuth2User == null) {
            return ResponseEntity.ok(Map.of("loggedIn", false));
        }

        String googleId = oAuth2User.getAttribute("sub");
        User user = userRepository.findByGoogleId(googleId).orElseThrow();

        return ResponseEntity.ok(Map.of(
            "loggedIn", true,
            "name", user.getName(),
            "email", user.getEmail()
        ));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal OAuth2User oAuth2User,
                                              HttpServletRequest request, HttpServletResponse response) {
        String googleId = oAuth2User.getAttribute("sub");
        userRepository.findByGoogleId(googleId).ifPresent(userRepository::delete);

        SecurityContextHolder.clearContext();
        request.getSession().invalidate();

        return ResponseEntity.noContent().build();
    }
}
