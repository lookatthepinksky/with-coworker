package com.devksg.withcoworkers.controller;

import com.devksg.withcoworkers.config.CustomUserDetails;
import com.devksg.withcoworkers.config.JwtTokenProvider;
import com.devksg.withcoworkers.domain.AuthProvider;
import com.devksg.withcoworkers.domain.ProviderType;
import com.devksg.withcoworkers.domain.User;
import com.devksg.withcoworkers.dto.LoginRequest;
import com.devksg.withcoworkers.dto.SignupRequest;
import com.devksg.withcoworkers.repository.AuthProviderRepository;
import com.devksg.withcoworkers.repository.TeamMemberRepository;
import com.devksg.withcoworkers.repository.UserRepository;
import com.devksg.withcoworkers.service.LoginAttemptService;
import com.devksg.withcoworkers.service.UserSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final UserSessionService userSessionService;
    private final AuthenticationManager authenticationManager;
    private final LoginAttemptService loginAttemptService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (authProviderRepository.findByProviderAndProviderId(ProviderType.LOCAL, request.getLoginId()).isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "존재하지 않는 아이디입니다."));
        }
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getLoginId(), request.getPassword())
            );
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            User user = userDetails.getUser();
            loginAttemptService.recordSuccess(request.getLoginId());
            String token = jwtTokenProvider.generateToken(user.getId());
            userSessionService.save(user.getId(), token);
            boolean isExistingMember = teamMemberRepository.existsByUserId(user.getId());
            String redirectPath = isExistingMember ? "/dashboard" : "/team-select";
            return ResponseEntity.ok(Map.of("token", token, "redirectPath", redirectPath));
        } catch (LockedException e) {
            return ResponseEntity.status(429).body(Map.of("message", "로그인 시도 횟수를 초과했습니다. 10분 후 다시 시도해주세요."));
        } catch (BadCredentialsException e) {
            loginAttemptService.recordFailure(request.getLoginId());
            int remaining = loginAttemptService.getRemainingAttempts(request.getLoginId());
            String message = remaining > 0
                    ? "아이디 또는 비밀번호가 올바르지 않습니다. (남은 시도: " + remaining + "회)"
                    : "로그인 시도 횟수를 초과했습니다. 10분 후 다시 시도해주세요.";
            return ResponseEntity.status(401).body(Map.of("message", message));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal User user) {
        if (user != null) {
            userSessionService.invalidate(user.getId());
        }
        return ResponseEntity.ok(Map.of("message", "로그아웃되었습니다."));
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
