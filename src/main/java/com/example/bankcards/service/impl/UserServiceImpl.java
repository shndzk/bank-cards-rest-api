package com.example.bankcards.service.impl;

import com.example.bankcards.dto.UserPageResponseDto;
import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserPageResponseDto getAllUsers(int page, int size) {
        Page<User> userPage = userRepository.findAll(PageRequest.of(page, size));

        UserPageResponseDto responseDto = new UserPageResponseDto();
        responseDto.setContent(userPage.getContent().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList()));
        responseDto.setTotalElements(userPage.getTotalElements());
        responseDto.setTotalPages(userPage.getTotalPages());

        return responseDto;
    }

    @Override
    @Transactional
    public void toggleUserBlock(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        if ("admin".equals(user.getUsername())) {
            throw new IllegalArgumentException("Нельзя заблокировать учетную запись главного администратора");
        }

        user.setBlocked(!user.isBlocked());
        userRepository.save(user);
    }

    private UserResponseDto mapToResponseDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole().name());
        return dto;
    }
}
