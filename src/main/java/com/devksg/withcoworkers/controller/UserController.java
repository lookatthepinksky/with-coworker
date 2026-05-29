package com.devksg.withcoworkers.controller;

import com.devksg.withcoworkers.domain.User;
import com.devksg.withcoworkers.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.ok(Map.of("loggedIn", false));
        }
        return ResponseEntity.ok(Map.of(
            "loggedIn", true,
            "name", user.getName(),
            "email", user.getEmail() != null ? user.getEmail() : ""
        ));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal User user) {
        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }
}
