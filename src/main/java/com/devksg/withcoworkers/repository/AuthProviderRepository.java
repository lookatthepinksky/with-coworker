package com.devksg.withcoworker.repository;

import com.devksg.withcoworker.domain.AuthProvider;
import com.devksg.withcoworker.domain.ProviderType;
import com.devksg.withcoworker.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthProviderRepository extends JpaRepository<AuthProvider, Long> {
    Optional<AuthProvider> findByProviderAndProviderId(ProviderType provider, String providerId);
    Optional<AuthProvider> findFirstByUser(User user);
}
