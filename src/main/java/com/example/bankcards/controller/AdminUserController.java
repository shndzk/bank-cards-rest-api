package com.example.bankcards.controller;

import com.example.bankcards.controller.api.AdminUserControllerApi;
import com.example.bankcards.dto.UserPageResponseDto;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminUserController implements AdminUserControllerApi {

    private final UserService userService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserPageResponseDto> getAllUsers(Integer page, Integer size) {
        int pageParam = (page != null) ? page : 0;
        int sizeParam = (size != null) ? size : 10;

        UserPageResponseDto response = userService.getAllUsers(pageParam, sizeParam);
        return ResponseEntity.ok(response);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> toggleUserBlock(Long id) {
        userService.toggleUserBlock(id);
        return ResponseEntity.ok().build();
    }
}
