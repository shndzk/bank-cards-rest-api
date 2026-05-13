package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardOperationException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.repository.BankCardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private BankCardRepository bankCardRepository;

    @InjectMocks
    private CardService cardService;

    private User testUser;
    private User anotherUser;
    private BankCard fromCard;
    private BankCard toCard;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("user_owner");

        anotherUser = new User();
        anotherUser.setUsername("other_user");

        fromCard = new BankCard();
        fromCard.setId(1L);
        fromCard.setOwner(testUser);
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setBalance(new BigDecimal("1000.00"));

        toCard = new BankCard();
        toCard.setId(2L);
        toCard.setOwner(testUser);
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setBalance(new BigDecimal("200.00"));
    }


    @Test
    @DisplayName("Успешный перевод между своими картами")
    void transferBetweenOwnCards_Success() {
        TransferRequestDto dto = new TransferRequestDto();
        dto.setFromCardId(1L);
        dto.setToCardId(2L);
        dto.setAmount(new BigDecimal("300.00"));

        when(bankCardRepository.findByIdAndOwnerUsername(1L, "user_owner")).thenReturn(Optional.of(fromCard));
        when(bankCardRepository.findByIdAndOwnerUsername(2L, "user_owner")).thenReturn(Optional.of(toCard));

        cardService.transferBetweenOwnCards("user_owner", dto);

        assertEquals(new BigDecimal("700.00"), fromCard.getBalance());
        assertEquals(new BigDecimal("500.00"), toCard.getBalance());
        verify(bankCardRepository, times(1)).save(fromCard);
        verify(bankCardRepository, times(1)).save(toCard);
    }

    @Test
    @DisplayName("Ошибка перевода: Недостаточно средств на карте списания")
    void transferBetweenOwnCards_InsufficientFunds() {
        TransferRequestDto dto = new TransferRequestDto();
        dto.setFromCardId(1L);
        dto.setToCardId(2L);
        dto.setAmount(new BigDecimal("1500.00"));

        when(bankCardRepository.findByIdAndOwnerUsername(1L, "user_owner")).thenReturn(Optional.of(fromCard));
        when(bankCardRepository.findByIdAndOwnerUsername(2L, "user_owner")).thenReturn(Optional.of(toCard));

        assertThrows(InsufficientFundsException.class, () ->
                cardService.transferBetweenOwnCards("user_owner", dto)
        );
        verify(bankCardRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ошибка перевода: Сумма перевода меньше или равна нулю")
    void transferBetweenOwnCards_NegativeAmount() {
        TransferRequestDto dto = new TransferRequestDto();
        dto.setFromCardId(1L);
        dto.setToCardId(2L);
        dto.setAmount(new BigDecimal("-50.00"));

        assertThrows(IllegalArgumentException.class, () ->
                cardService.transferBetweenOwnCards("user_owner", dto)
        );
    }


    @Test
    @DisplayName("USER: Успешная блокировка собственной карты")
    void updateCardStatus_UserBlocksOwnCard_Success() {
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(fromCard));

        cardService.updateCardStatus(1L, "BLOCKED", "user_owner", "ROLE_USER");

        assertEquals(CardStatus.BLOCKED, fromCard.getStatus());
        verify(bankCardRepository, times(1)).save(fromCard);
    }

    @Test
    @DisplayName("USER: Ошибка при попытке изменить статус чужой карты")
    void updateCardStatus_UserChangesAlienCard_ThrowsException() {
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(fromCard));

        assertThrows(CardOperationException.class, () ->
                cardService.updateCardStatus(1L, "BLOCKED", "other_user", "ROLE_USER")
        );
        verify(bankCardRepository, never()).save(any());
    }

    @Test
    @DisplayName("USER: Ошибка при попытке активировать карту (USER может только блокировать)")
    void updateCardStatus_UserTriesToActivate_ThrowsException() {
        fromCard.setStatus(CardStatus.BLOCKED);
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(fromCard));

        assertThrows(CardOperationException.class, () ->
                cardService.updateCardStatus(1L, "ACTIVE", "user_owner", "ROLE_USER")
        );
        verify(bankCardRepository, never()).save(any());
    }

    @Test
    @DisplayName("ADMIN: Успешная активация/изменение статуса любой карты")
    void updateCardStatus_AdminChangesAnyCard_Success() {
        fromCard.setStatus(CardStatus.BLOCKED);
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(fromCard));

        cardService.updateCardStatus(1L, "ACTIVE", "admin_user", "ROLE_ADMIN");

        assertEquals(CardStatus.ACTIVE, fromCard.getStatus());
        verify(bankCardRepository, times(1)).save(fromCard);
    }
}
