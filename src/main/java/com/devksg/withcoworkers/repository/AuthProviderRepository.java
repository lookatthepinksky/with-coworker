package com.devksg.withcoworkers.repository;

import com.devksg.withcoworkers.domain.AuthProvider;
import com.devksg.withcoworkers.domain.ProviderType;
import com.devksg.withcoworkers.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AuthProviderRepository extends JpaRepository<AuthProvider, Long> {

    @Query("SELECT ap FROM AuthProvider ap JOIN FETCH ap.user WHERE ap.provider = :provider AND ap.providerId = :providerId")
    Optional<AuthProvider> findByProviderAndProviderId(@Param("provider") ProviderType provider, @Param("providerId") String providerId);
    Optional<AuthProvider> findFirstByUser(User user);
}
