package com.example.bankcards.security;

import com.example.bankcards.exception.CardOperationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public AuthPrincipal current() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated() || !(a.getPrincipal() instanceof AuthPrincipal p)) {
            throw new CardOperationException("Доступ запрещен: пользователь не авторизован");
        }
        return p;
    }

    public Long currentUserId() { return current().userId(); }
    public String currentUsername() { return current().username(); }
    public boolean isAdmin() { return current().isAdmin(); }
}
