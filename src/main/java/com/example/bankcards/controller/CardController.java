package com.example.bankcards.controller;

import com.example.bankcards.controller.api.CardControllerApi;
import com.example.bankcards.dto.CardCreateDto;
import com.example.bankcards.dto.CardPageResponseDto;
import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.dto.UpdateCardStatusRequest;
import com.example.bankcards.security.CurrentUserService;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CardController implements CardControllerApi {

    private final CardService cardService;
    private final CurrentUserService currentUserService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponseDto> createCard(@Valid CardCreateDto cardCreateDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.createCard(cardCreateDto));
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<CardPageResponseDto> getAllCards(Integer page, Integer size, String cardholder) {
        String username = currentUserService.currentUsername();
        String role = currentUserService.current().role();

        int pageParam = (page != null) ? page : 0;
        int sizeParam = (size != null) ? size : 10;

        CardPageResponseDto cards = cardService.getCardsList(username, role, pageParam, sizeParam, cardholder);
        return ResponseEntity.ok(cards);
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> updateCardStatus(Long id, @Valid UpdateCardStatusRequest updateCardStatusRequest) {
        String username = currentUserService.currentUsername();
        String role = currentUserService.current().role();

        String statusStr = updateCardStatusRequest.getStatus().getValue();

        cardService.updateCardStatus(id, statusStr, username, role);
        return ResponseEntity.ok().build();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> transferBetweenOwnCards(@Valid TransferRequestDto transferRequestDto) {
        String username = currentUserService.currentUsername();
        cardService.transferBetweenOwnCards(username, transferRequestDto);
        return ResponseEntity.ok().build();
    }
}
