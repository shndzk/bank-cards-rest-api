package com.example.bankcards.service;

import com.example.bankcards.dto.UserPageResponseDto;

public interface UserService {
    UserPageResponseDto getAllUsers(int page, int size);
    void toggleUserBlock(Long id);
}
