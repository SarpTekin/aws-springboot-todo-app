package com.microtodo.task_service.security;

import java.util.Collections;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final Long userId;
    private final String username;

    public JwtAuthenticationToken(Long userId, String username) {
        super(Collections.emptyList());
        this.userId = userId;
        this.username = username;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return new JwtPrincipal(userId, username);
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public static class JwtPrincipal {
        private final Long userId;
        private final String username;

        public JwtPrincipal(Long userId, String username) {
            this.userId = userId;
            this.username = username;
        }

        public Long getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }
    }
}

