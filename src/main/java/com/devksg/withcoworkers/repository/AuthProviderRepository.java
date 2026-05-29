package com.devksg.withcoworkers.repository;

import com.devksg.withcoworkers.domain.AuthProvider;
import com.devksg.withcoworkers.domain.ProviderType;
import com.devksg.withcoworkers.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthProviderRepository extends JpaRepository<AuthProvider, Long> {
    Optional<AuthProvider> findByProviderAndProviderId(ProviderType provider, String providerId);
    Optional<AuthProvider> findFirstByUser(User user);
}
