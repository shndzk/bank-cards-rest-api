package com.example.bankcards.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration; // ДОБАВЛЕНО
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .map(user -> org.springframework.security.core.userdetails.User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .roles(user.getRole().name().replace("ROLE_", ""))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден в БД: " + username));
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(c -> c.configurationSource(req -> {
                    var cfg = new org.springframework.web.cors.CorsConfiguration();
                    cfg.setAllowedOrigins(List.of("*"));
                    cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS","PATCH"));
                    cfg.setAllowedHeaders(List.of("*"));
                    return cfg;
                }))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/api/v1/auth/**", "/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/v1/cards").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/cards/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/cards/transfer").hasRole("USER")
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        .anyRequest().authenticated())
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint((req, res, ex) -> {
                            res.setStatus(HttpStatus.UNAUTHORIZED.value());
                            res.setContentType(MediaType.APPLICATION_JSON_VALUE);

                            Map<String, Object> errorDetails = new HashMap<>();
                            errorDetails.put("timestamp", LocalDateTime.now().toString());
                            errorDetails.put("status", HttpStatus.UNAUTHORIZED.value());
                            errorDetails.put("error", "Unauthorized");
                            errorDetails.put("message", "Не авторизован или неверный токен");

                            objectMapper.writeValue(res.getOutputStream(), errorDetails);
                        })
                        .accessDeniedHandler((req, res, ex) -> {
                            res.setStatus(HttpStatus.FORBIDDEN.value());
                            res.setContentType(MediaType.APPLICATION_JSON_VALUE);

                            Map<String, Object> errorDetails = new HashMap<>();
                            errorDetails.put("timestamp", LocalDateTime.now().toString());
                            errorDetails.put("status", HttpStatus.FORBIDDEN.value());
                            errorDetails.put("error", "Forbidden");
                            errorDetails.put("message", "Доступ запрещён: недостаточно прав");

                            objectMapper.writeValue(res.getOutputStream(), errorDetails);
                        }))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
