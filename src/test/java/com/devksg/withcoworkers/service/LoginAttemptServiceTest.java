package com.devksg.withcoworkers.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * LoginAttemptService의 로그인 실패 횟수 관리 및 잠금 로직을 테스트하는 클래스.
 *
 * LoginAttemptService는 Redis를 이용해 로그인 실패 횟수를 저장하고,
 * 5회 이상 실패 시 계정을 10분간 잠금하는 역할을 한다.
 *
 * @ExtendWith(MockitoExtension.class): 스프링 컨텍스트 없이 Mockito만으로 테스트한다.
 *   → 가볍고 빠르며, 단순 비즈니스 로직 검증에 적합하다.
 *
 * @MockitoSettings(strictness = LENIENT): 테스트에서 사용하지 않는 Mock 설정이 있어도
 *   불필요한 stubbing 경고를 발생시키지 않는다.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LoginAttemptServiceTest {

    // @Mock: 실제 Redis에 연결하지 않고 가짜 RedisTemplate을 사용한다.
    @Mock
    private StringRedisTemplate redisTemplate;

    // Redis의 문자열 값 조작(get/set/increment)을 담당하는 Mock 객체
    @Mock
    private ValueOperations<String, String> valueOperations;

    // @InjectMocks: 위의 @Mock 객체들을 주입받은 실제 서비스 인스턴스를 생성한다.
    @InjectMocks
    private LoginAttemptService loginAttemptService;

    private static final String LOGIN_ID = "testuser";
    // Redis에 저장되는 실패 횟수 키 형식: "login:fail:{loginId}"
    private static final String KEY = "login:fail:testuser";
    // 잠금 지속 시간: 10분
    private static final Duration LOCK_DURATION = Duration.ofMinutes(10);

    /**
     * 각 테스트 실행 전: redisTemplate.opsForValue() 호출 시 Mock valueOperations를 반환하도록 설정.
     * 실제 Redis 없이 valueOperations의 동작을 제어할 수 있게 된다.
     */
    @BeforeEach
    void setUp() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
    }

    // ============================================================
    // recordFailure: 로그인 실패 기록
    // ============================================================

    @Test
    @DisplayName("1회 실패: 카운트 증가 + TTL 10분 설정")
    void recordFailure_firstAttempt_incrementsAndSetsTtl() {
        // increment()가 1을 반환 → 첫 번째 실패
        given(valueOperations.increment(KEY)).willReturn(1L);

        loginAttemptService.recordFailure(LOGIN_ID);

        // 카운트가 1 증가했는지 확인
        verify(valueOperations).increment(KEY);
        // 첫 실패 시 TTL(만료 시간)을 10분으로 설정해야 함 (키가 자동 삭제되도록)
        verify(redisTemplate).expire(KEY, LOCK_DURATION);
    }

    @Test
    @DisplayName("2~4회 실패: 카운트만 증가, TTL 재설정 없음")
    void recordFailure_intermediateAttempts_incrementsWithoutTtlReset() {
        // increment()가 3을 반환 → 중간 실패 (2~4회)
        given(valueOperations.increment(KEY)).willReturn(3L);

        loginAttemptService.recordFailure(LOGIN_ID);

        verify(valueOperations).increment(KEY);
        // 중간 실패는 TTL을 건드리지 않아야 함 (기존 만료 시간 유지)
        verify(redisTemplate, never()).expire(any(), any(Duration.class));
    }

    @Test
    @DisplayName("5회 실패: 카운트 증가 + TTL 10분 갱신 (잠금 시작)")
    void recordFailure_fifthAttempt_incrementsAndResetsTtl() {
        // increment()가 5를 반환 → 5번째 실패 (잠금 기준 도달)
        given(valueOperations.increment(KEY)).willReturn(5L);

        loginAttemptService.recordFailure(LOGIN_ID);

        verify(valueOperations).increment(KEY);
        // 5회 실패 시 TTL을 10분으로 갱신하여 잠금 해제 시점을 현재 기준으로 재설정
        verify(redisTemplate).expire(KEY, LOCK_DURATION);
    }

    // ============================================================
    // recordSuccess: 로그인 성공 처리
    // ============================================================

    @Test
    @DisplayName("로그인 성공: 실패 카운트 키 삭제")
    void recordSuccess_deletesFailKey() {
        loginAttemptService.recordSuccess(LOGIN_ID);

        // 로그인 성공 시 Redis에서 실패 횟수 키를 삭제해야 함 (초기화)
        verify(redisTemplate).delete(KEY);
    }

    // ============================================================
    // isBlocked: 계정 잠금 여부 확인
    // ============================================================

    @Test
    @DisplayName("Redis 키 없음 → isBlocked() = false")
    void isBlocked_noKey_returnsFalse() {
        // Redis에 키가 없으면 null 반환 → 실패 기록이 없으므로 잠금 아님
        given(valueOperations.get(KEY)).willReturn(null);

        assertThat(loginAttemptService.isBlocked(LOGIN_ID)).isFalse();
    }

    @Test
    @DisplayName("실패 4회 → isBlocked() = false (아직 잠금 전)")
    void isBlocked_countBelowMax_returnsFalse() {
        // 실패 횟수가 4회 → 최대(5회) 미만이므로 잠금 아님
        given(valueOperations.get(KEY)).willReturn("4");

        assertThat(loginAttemptService.isBlocked(LOGIN_ID)).isFalse();
    }

    @Test
    @DisplayName("실패 5회 → isBlocked() = true (잠금 기준 충족)")
    void isBlocked_countAtMax_returnsTrue() {
        // 실패 횟수가 정확히 5회 → 잠금 기준 도달
        given(valueOperations.get(KEY)).willReturn("5");

        assertThat(loginAttemptService.isBlocked(LOGIN_ID)).isTrue();
    }

    @Test
    @DisplayName("실패 5회 초과 → isBlocked() = true")
    void isBlocked_countAboveMax_returnsTrue() {
        // 실패 횟수가 5 초과 (예: 9회) → 여전히 잠금 상태
        given(valueOperations.get(KEY)).willReturn("9");

        assertThat(loginAttemptService.isBlocked(LOGIN_ID)).isTrue();
    }

    // ============================================================
    // getRemainingAttempts: 남은 로그인 시도 횟수 반환
    // ============================================================

    @Test
    @DisplayName("Redis 키 없음 → 남은 시도 횟수 = 5 (최대)")
    void getRemainingAttempts_noKey_returnsMax() {
        // 실패 기록이 없으면 최대 횟수(5회) 그대로 반환
        given(valueOperations.get(KEY)).willReturn(null);

        assertThat(loginAttemptService.getRemainingAttempts(LOGIN_ID)).isEqualTo(5);
    }

    @Test
    @DisplayName("실패 1회 → 남은 시도 횟수 = 4")
    void getRemainingAttempts_oneFailure_returnsFour() {
        given(valueOperations.get(KEY)).willReturn("1");

        // 5 - 1 = 4
        assertThat(loginAttemptService.getRemainingAttempts(LOGIN_ID)).isEqualTo(4);
    }

    @Test
    @DisplayName("실패 2회 → 남은 시도 횟수 = 3")
    void getRemainingAttempts_twoFailures_returnsThree() {
        given(valueOperations.get(KEY)).willReturn("2");

        // 5 - 2 = 3
        assertThat(loginAttemptService.getRemainingAttempts(LOGIN_ID)).isEqualTo(3);
    }

    @Test
    @DisplayName("실패 4회 → 남은 시도 횟수 = 1")
    void getRemainingAttempts_fourFailures_returnsOne() {
        given(valueOperations.get(KEY)).willReturn("4");

        // 5 - 4 = 1
        assertThat(loginAttemptService.getRemainingAttempts(LOGIN_ID)).isEqualTo(1);
    }

    @Test
    @DisplayName("실패 5회 → 남은 시도 횟수 = 0 (잠금)")
    void getRemainingAttempts_fiveFailures_returnsZero() {
        given(valueOperations.get(KEY)).willReturn("5");

        // 5 - 5 = 0, 더 이상 시도 불가
        assertThat(loginAttemptService.getRemainingAttempts(LOGIN_ID)).isEqualTo(0);
    }
}
