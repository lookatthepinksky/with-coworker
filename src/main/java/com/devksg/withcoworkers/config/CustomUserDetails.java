package com.devksg.withcoworkers.config;

import com.devksg.withcoworkers.domain.AuthProvider;
import com.devksg.withcoworkers.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final AuthProvider authProvider;
    private final boolean accountNonLocked;

    public CustomUserDetails(AuthProvider authProvider, boolean accountNonLocked) {
        this.authProvider = authProvider;
        this.accountNonLocked = accountNonLocked;
    }

    public User getUser() {
        return authProvider.getUser();
    }

    @Override public String getPassword() { return authProvider.getPasswordHash(); }
    @Override public String getUsername() { return authProvider.getProviderId(); }
    @Override public boolean isAccountNonLocked() { return accountNonLocked; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return Collections.emptyList(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
