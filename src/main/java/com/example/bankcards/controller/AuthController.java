package com.example.bankcards.controller;

import com.example.bankcards.controller.api.AuthControllerApi;
import com.example.bankcards.dto.AuthRequestDto;
import com.example.bankcards.dto.AuthResponseDto;
import com.example.bankcards.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthController implements AuthControllerApi {

    private final AuthService authService;

    @Override
    public ResponseEntity<AuthResponseDto> login(AuthRequestDto authRequestDto) {
        AuthResponseDto response = authService.login(authRequestDto);
        return ResponseEntity.ok(response);
    }
}
