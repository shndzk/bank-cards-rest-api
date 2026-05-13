package com.example.bankcards.security;

public record AuthPrincipal(Long userId, String username, String role) {
    public boolean isAdmin() {
        return "ROLE_ADMIN".equals(role) || "ADMIN".equals(role);
    }
}
