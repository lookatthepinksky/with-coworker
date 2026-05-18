package com.devksg.withcoworker.controller;

import com.devksg.withcoworker.config.JwtTokenProvider;
import com.devksg.withcoworker.domain.AuthProvider;
import com.devksg.withcoworker.domain.ProviderType;
import com.devksg.withcoworker.domain.User;
import com.devksg.withcoworker.dto.LoginRequest;
import com.devksg.withcoworker.dto.SignupRequest;
import com.devksg.withcoworker.repository.AuthProviderRepository;
import com.devksg.withcoworker.repository.TeamMemberRepository;
import com.devksg.withcoworker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final AuthProviderRepository authProviderRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    @Transactional
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        var found = authProviderRepository.findByProviderAndProviderId(ProviderType.LOCAL, request.getLoginId());

        return found
            .filter(ap -> passwordEncoder.matches(request.getPassword(), ap.getPasswordHash()))
            .map(ap -> {
                User user = ap.getUser();
                String token = jwtTokenProvider.generateToken(user.getId());
                boolean isExistingMember = teamMemberRepository.existsByUserId(user.getId());
                String redirectPath = isExistingMember ? "/dashboard" : "/team-select";
                return ResponseEntity.ok(Map.of("token", token, "redirectPath", redirectPath));
            })
            .orElse(ResponseEntity.status(401).body(Map.of("message", "아이디 또는 비밀번호가 올바르지 않습니다.")));
    }

    @PostMapping("/signup")
    @Transactional
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        if (authProviderRepository.findByProviderAndProviderId(ProviderType.LOCAL, request.getLoginId()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "이미 사용 중인 아이디입니다."));
        }
        if (request.getEmail() != null && userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "이미 사용 중인 이메일입니다."));
        }

        User user = userRepository.save(User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .build());

        authProviderRepository.save(AuthProvider.builder()
            .user(user)
            .provider(ProviderType.LOCAL)
            .providerId(request.getLoginId())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .build());

        return ResponseEntity.ok(Map.of("message", "회원가입이 완료되었습니다."));
    }
}
