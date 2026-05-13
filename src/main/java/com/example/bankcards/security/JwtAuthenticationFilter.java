package com.example.bankcards.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ") && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = auth.substring(7);
            try {
                Claims c = jwtTokenProvider.parse(token);
                Long uid = c.get("uid", Long.class);
                String username = c.getSubject();
                String role = c.get("role", String.class);

                String finalRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;

                AuthPrincipal principal = new AuthPrincipal(uid, username, finalRole);
                var authentication = new UsernamePasswordAuthenticationToken(
                        principal, null, List.of(new SimpleGrantedAuthority(finalRole)));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                System.err.println("Ошибка валидации JWT-токена: " + e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
