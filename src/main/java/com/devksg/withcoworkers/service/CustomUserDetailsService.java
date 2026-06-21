package com.devksg.withcoworkers.service;

import com.devksg.withcoworkers.config.CustomUserDetails;
import com.devksg.withcoworkers.domain.AuthProvider;
import com.devksg.withcoworkers.domain.ProviderType;
import com.devksg.withcoworkers.repository.AuthProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AuthProviderRepository authProviderRepository;
    private final LoginAttemptService loginAttemptService;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        AuthProvider ap = authProviderRepository
                .findByProviderAndProviderId(ProviderType.LOCAL, loginId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        boolean isBlocked = loginAttemptService.isBlocked(loginId);
        return new CustomUserDetails(ap, !isBlocked);
    }
}
