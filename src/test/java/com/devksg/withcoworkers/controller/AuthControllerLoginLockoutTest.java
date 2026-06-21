package com.devksg.withcoworkers.controller;

import com.devksg.withcoworkers.config.CustomUserDetails;
import com.devksg.withcoworkers.config.JwtTokenProvider;
import com.devksg.withcoworkers.domain.AuthProvider;
import com.devksg.withcoworkers.domain.User;
import com.devksg.withcoworkers.repository.AuthProviderRepository;
import com.devksg.withcoworkers.repository.TeamMemberRepository;
import com.devksg.withcoworkers.repository.UserRepository;
import com.devksg.withcoworkers.service.LoginAttemptService;
import com.devksg.withcoworkers.service.UserSessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController의 로그인 및 계정 잠금 기능을 테스트하는 클래스.
 *
 * @WebMvcTest: 실제 서버를 띄우지 않고 MockMvc로 HTTP 요청/응답만 테스트한다.
 *              controllers에 명시한 AuthController만 스프링 컨텍스트에 로드된다.
 *
 * excludeAutoConfiguration: Spring Security와 OAuth2 자동설정을 제외한다.
 *   - 테스트에서 Security 필터가 개입하면 인증 없는 요청이 모두 막혀버리므로
 *     Security 설정을 꺼두고 컨트롤러 로직 자체만 검증한다.
 */
@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class,
                OAuth2ClientWebSecurityAutoConfiguration.class
        }
)
class AuthControllerLoginLockoutTest {

    // MockMvc: 실제 HTTP 요청처럼 컨트롤러를 호출할 수 있는 테스트 도구
    @Autowired
    private MockMvc mockMvc;

    // ObjectMapper: Java 객체 ↔ JSON 문자열 변환에 사용
    @Autowired
    private ObjectMapper objectMapper;

    // @MockitoBean: 실제 Bean 대신 Mock(가짜) 객체를 스프링 컨텍스트에 등록한다.
    // 실제 DB, Redis 등에 연결하지 않고 원하는 반환값을 임의로 지정할 수 있다.
    @MockitoBean private AuthenticationManager authenticationManager; // 인증 처리 담당
    @MockitoBean private LoginAttemptService loginAttemptService;     // 로그인 실패 횟수/잠금 관리
    @MockitoBean private UserRepository userRepository;
    @MockitoBean private AuthProviderRepository authProviderRepository;
    @MockitoBean private TeamMemberRepository teamMemberRepository;   // 팀 가입 여부 확인
    @MockitoBean private JwtTokenProvider jwtTokenProvider;           // JWT 토큰 생성
    @MockitoBean private PasswordEncoder passwordEncoder;
    @MockitoBean private UserSessionService userSessionService;

    private static final String URL = "/api/auth/login"; // 로그인 API 엔드포인트
    private static final String LOGIN_ID = "testuser";   // 테스트에 공통으로 사용할 아이디

    /**
     * 로그인 요청 본문(JSON)을 생성하는 헬퍼 메서드.
     * {"loginId": "...", "password": "..."} 형태의 JSON 문자열을 반환한다.
     */
    private String loginBody(String loginId, String password) throws Exception {
        return objectMapper.writeValueAsString(Map.of("loginId", loginId, "password", password));
    }

    // ============================================================
    // 로그인 성공
    // ============================================================

    @Test
    @DisplayName("로그인 성공: 200 + 토큰 반환, 실패 카운트 초기화")
    void login_success_returns200AndClearsFailCount() throws Exception {
        // --- 가짜 User, AuthProvider 객체 준비 ---
        User mockUser = mock(User.class);
        given(mockUser.getId()).willReturn(1L); // userId = 1

        AuthProvider mockAuthProvider = mock(AuthProvider.class);
        given(mockAuthProvider.getUser()).willReturn(mockUser);
        given(mockAuthProvider.getPasswordHash()).willReturn("$2a$10$hashed");
        given(mockAuthProvider.getProviderId()).willReturn(LOGIN_ID);

        // CustomUserDetails: Spring Security가 인증 후 반환하는 사용자 정보 객체
        CustomUserDetails userDetails = new CustomUserDetails(mockAuthProvider, true);
        Authentication auth = mock(Authentication.class);
        given(auth.getPrincipal()).willReturn(userDetails); // 인증 결과에서 사용자 정보 반환

        // authenticationManager.authenticate()가 정상 인증 객체를 반환하도록 설정 (인증 성공)
        given(authenticationManager.authenticate(any())).willReturn(auth);
        // JWT 토큰 생성 Mock
        given(jwtTokenProvider.generateToken(1L)).willReturn("jwt-token");
        // 팀에 가입된 사용자 → /dashboard로 이동
        given(teamMemberRepository.existsByUserId(1L)).willReturn(true);

        // --- 실제 요청 수행 및 검증 ---
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(LOGIN_ID, "correctPw")))
                .andExpect(status().isOk())                              // HTTP 200
                .andExpect(jsonPath("$.token").value("jwt-token"))       // 토큰 반환 확인
                .andExpect(jsonPath("$.redirectPath").value("/dashboard")); // 리다이렉트 경로 확인

        // 로그인 성공 시 실패 카운트를 초기화해야 하고, 실패 기록은 하면 안 됨
        verify(loginAttemptService).recordSuccess(LOGIN_ID);
        verify(loginAttemptService, never()).recordFailure(any());
    }

    @Test
    @DisplayName("로그인 성공 후 팀 미가입 → redirectPath = /team-select")
    void login_success_noTeam_redirectsToTeamSelect() throws Exception {
        User mockUser = mock(User.class);
        given(mockUser.getId()).willReturn(2L);

        AuthProvider mockAuthProvider = mock(AuthProvider.class);
        given(mockAuthProvider.getUser()).willReturn(mockUser);
        given(mockAuthProvider.getPasswordHash()).willReturn("$2a$10$hashed");
        given(mockAuthProvider.getProviderId()).willReturn(LOGIN_ID);

        CustomUserDetails userDetails = new CustomUserDetails(mockAuthProvider, true);
        Authentication auth = mock(Authentication.class);
        given(auth.getPrincipal()).willReturn(userDetails);

        given(authenticationManager.authenticate(any())).willReturn(auth);
        given(jwtTokenProvider.generateToken(2L)).willReturn("jwt-token");
        // 팀에 미가입된 사용자 → /team-select로 이동
        given(teamMemberRepository.existsByUserId(2L)).willReturn(false);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(LOGIN_ID, "correctPw")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.redirectPath").value("/team-select")); // 팀 선택 페이지로 이동
    }

    // ============================================================
    // 비밀번호 오류 (잠금 전)
    // ============================================================

    @Test
    @DisplayName("1회 틀림 → 401, 남은 시도 4회 메시지")
    void login_firstWrongPassword_returns401WithRemaining4() throws Exception {
        // 인증 실패 시 BadCredentialsException 발생하도록 설정 (비밀번호 불일치)
        given(authenticationManager.authenticate(any()))
                .willThrow(new BadCredentialsException("bad credentials"));
        // 이 시점의 남은 시도 횟수는 4회
        given(loginAttemptService.getRemainingAttempts(LOGIN_ID)).willReturn(4);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(LOGIN_ID, "wrong")))
                .andExpect(status().isUnauthorized()) // HTTP 401
                .andExpect(jsonPath("$.message")
                        .value("아이디 또는 비밀번호가 올바르지 않습니다. (남은 시도: 4회)"));

        // 실패했으므로 recordFailure가 반드시 호출되어야 함
        verify(loginAttemptService).recordFailure(LOGIN_ID);
    }

    @Test
    @DisplayName("4회 틀림 → 401, 남은 시도 1회 메시지")
    void login_fourthWrongPassword_returns401WithRemaining1() throws Exception {
        given(authenticationManager.authenticate(any()))
                .willThrow(new BadCredentialsException("bad credentials"));
        // 이 시점의 남은 시도 횟수는 1회 (마지막 기회)
        given(loginAttemptService.getRemainingAttempts(LOGIN_ID)).willReturn(1);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(LOGIN_ID, "wrong")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("아이디 또는 비밀번호가 올바르지 않습니다. (남은 시도: 1회)"));

        verify(loginAttemptService).recordFailure(LOGIN_ID);
    }

    // ============================================================
    // 5번째 틀렸을 때 (잠금 진입)
    // ============================================================

    @Test
    @DisplayName("5회 틀림 → 401, 잠금 안내 메시지 (남은 시도 0)")
    void login_fifthWrongPassword_returns401WithLockoutMessage() throws Exception {
        given(authenticationManager.authenticate(any()))
                .willThrow(new BadCredentialsException("bad credentials"));
        // 남은 시도 횟수가 0 → 잠금 안내 메시지로 전환
        given(loginAttemptService.getRemainingAttempts(LOGIN_ID)).willReturn(0);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(LOGIN_ID, "wrong")))
                .andExpect(status().isUnauthorized())
                // 남은 횟수 대신 잠금 안내 메시지를 반환해야 함
                .andExpect(jsonPath("$.message")
                        .value("로그인 시도 횟수를 초과했습니다. 10분 후 다시 시도해주세요."));

        verify(loginAttemptService).recordFailure(LOGIN_ID);
    }

    // ============================================================
    // 이미 잠금 상태 (6회 이상 시도)
    // ============================================================

    @Test
    @DisplayName("잠금 상태에서 시도 → 429, 잠금 안내 메시지")
    void login_alreadyLocked_returns429() throws Exception {
        // Spring Security가 loadUserByUsername에서 isAccountNonLocked=false 감지 후 LockedException throw
        // → BadCredentialsException이 아닌 LockedException으로 잠금 여부를 구분한다.
        given(authenticationManager.authenticate(any()))
                .willThrow(new LockedException("account is locked"));

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(LOGIN_ID, "any")))
                // 잠금 상태는 429 Too Many Requests로 응답 (일반 인증 실패 401과 구별)
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message")
                        .value("로그인 시도 횟수를 초과했습니다. 10분 후 다시 시도해주세요."));

        // 이미 잠금 상태이므로 추가로 실패 카운트를 올리면 안 됨
        verify(loginAttemptService, never()).recordFailure(any());
    }

    // ============================================================
    // 5회 연속 실패 → 잠금 흐름 시나리오 테스트
    // ============================================================

    @Test
    @DisplayName("5회 연속 실패 → 각 시도마다 남은 횟수 감소 → 5번째는 잠금 메시지")
    void login_fiveConsecutiveFailures_lockoutSequence() throws Exception {
        // 매번 BadCredentialsException 발생 (비밀번호 계속 틀림)
        given(authenticationManager.authenticate(any()))
                .willThrow(new BadCredentialsException("bad credentials"));

        // willReturn(a, b, c, ...): 호출 순서에 따라 다른 값을 반환한다.
        // 1번째 호출 → 4, 2번째 → 3, 3번째 → 2, 4번째 → 1, 5번째 → 0
        given(loginAttemptService.getRemainingAttempts(LOGIN_ID))
                .willReturn(4, 3, 2, 1, 0);

        // 1회 실패: 남은 시도 4회
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(LOGIN_ID, "wrong")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("아이디 또는 비밀번호가 올바르지 않습니다. (남은 시도: 4회)"));

        // 2회 실패: 남은 시도 3회
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(LOGIN_ID, "wrong")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("아이디 또는 비밀번호가 올바르지 않습니다. (남은 시도: 3회)"));

        // 3회 실패: 남은 시도 2회
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(LOGIN_ID, "wrong")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("아이디 또는 비밀번호가 올바르지 않습니다. (남은 시도: 2회)"));

        // 4회 실패: 남은 시도 1회
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(LOGIN_ID, "wrong")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("아이디 또는 비밀번호가 올바르지 않습니다. (남은 시도: 1회)"));

        // 5회 실패: 남은 시도 0 → 잠금 메시지로 전환
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(LOGIN_ID, "wrong")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("로그인 시도 횟수를 초과했습니다. 10분 후 다시 시도해주세요."));

        // 총 5번의 실패가 모두 기록되었는지 검증
        verify(loginAttemptService, times(5)).recordFailure(LOGIN_ID);
    }
}
