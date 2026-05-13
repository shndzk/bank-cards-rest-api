package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateDto;
import com.example.bankcards.dto.CardPageResponseDto;
import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardOperationException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardService {

    private final BankCardRepository bankCardRepository;
    private final UserRepository userRepository;

    @Transactional
    public CardResponseDto createCard(CardCreateDto dto) {
        User owner = userRepository.findById(dto.getOwnerId())
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        BankCard card = new BankCard();
        card.setCardNumber(dto.getCardNumber());
        card.setOwner(owner);
        card.setExpiryDate(dto.getExpiryDate());
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);

        BankCard savedCard = bankCardRepository.save(card);
        return mapToResponseDto(savedCard);
    }

    @Transactional(readOnly = true)
    public CardPageResponseDto getCardsList(String username, String role, int page, int size, String cardholder) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BankCard> cardsPage;

        if ("ROLE_ADMIN".equals(role)) {
            if (cardholder != null && !cardholder.isBlank()) {
                cardsPage = bankCardRepository.findByOwnerUsernameContainingIgnoreCase(cardholder, pageable);
            } else {
                cardsPage = bankCardRepository.findAll(pageable);
            }
        } else {
            cardsPage = bankCardRepository.findByOwnerUsername(username, pageable);
        }

        CardPageResponseDto responseDto = new CardPageResponseDto();
        responseDto.setContent(cardsPage.getContent().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList()));
        responseDto.setTotalElements(cardsPage.getTotalElements());
        responseDto.setTotalPages(cardsPage.getTotalPages());
        responseDto.setSize(cardsPage.getSize());
        responseDto.setNumber(cardsPage.getNumber());

        return responseDto;
    }

    @Transactional
    public void updateCardStatus(Long id, String statusStr, String username, String role) {
        BankCard card = bankCardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Карта не найдена"));

        CardStatus newStatus = CardStatus.valueOf(statusStr);

        if ("ROLE_USER".equals(role)) {
            if (!card.getOwner().getUsername().equals(username)) {
                throw new CardOperationException("Доступ запрещен: вы не являетесь владельцем этой карты");
            }
            if (newStatus != CardStatus.BLOCKED) {
                throw new CardOperationException("Операция отклонена: пользователю разрешена только блокировка карт");
            }
        }

        card.setStatus(newStatus);
        bankCardRepository.save(card);
    }

    @Transactional
    public void deleteCard(Long id) {
        if (!bankCardRepository.existsById(id)) {
            throw new EntityNotFoundException("Карта не найдена");
        }
        bankCardRepository.deleteById(id);
    }

    @Transactional
    public void transferBetweenOwnCards(String username, TransferRequestDto dto) {
        BigDecimal amount = dto.getAmount();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма перевода должна быть больше нуля");
        }

        BankCard fromCard = bankCardRepository.findByIdAndOwnerUsername(dto.getFromCardId(), username)
                .orElseThrow(() -> new EntityNotFoundException("Карта списания не найдена или вам не принадлежит"));

        BankCard toCard = bankCardRepository.findByIdAndOwnerUsername(dto.getToCardId(), username)
                .orElseThrow(() -> new EntityNotFoundException("Карта зачисления не найдена или вам не принадлежит"));

        if (fromCard.getStatus() != CardStatus.ACTIVE || toCard.getStatus() != CardStatus.ACTIVE) {
            throw new CardOperationException("Обе карты должны быть активны для выполнения операции");
        }

        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Недостаточно средств на карте списания");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        bankCardRepository.save(fromCard);
        bankCardRepository.save(toCard);
    }

    private CardResponseDto mapToResponseDto(BankCard card) {
        CardResponseDto dto = new CardResponseDto();
        dto.setId(card.getId());
        dto.setCardNumber(card.getCardNumber());
        dto.setOwnerUsername(card.getOwner().getUsername());

        dto.setExpiryDate(card.getExpiryDate());

        dto.setStatus(CardResponseDto.StatusEnum.valueOf(card.getStatus().name()));

        dto.setBalance(card.getBalance());

        return dto;
    }
}
