package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreateDto;
import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.security.CurrentUserService;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CardService cardService;

    @MockBean
    private CurrentUserService currentUserService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/cards — Успешное создание карты администратором")
    void createCard_Admin_Success() throws Exception {
        CardCreateDto createDto = new CardCreateDto();
        createDto.setCardNumber("1111222233334444");
        createDto.setOwnerId(2L);
        createDto.setExpiryDate(LocalDate.parse("2029-12-31"));

        CardResponseDto responseDto = new CardResponseDto();
        responseDto.setId(1L);
        responseDto.setCardNumber("**** **** **** 4444");
        responseDto.setBalance(BigDecimal.ZERO);

        when(cardService.createCard(any(CardCreateDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.cardNumber").value("**** **** **** 4444"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/v1/cards — Ошибка 403 при попытке создания карты пользователем")
    void createCard_User_Forbidden() throws Exception {
        CardCreateDto createDto = new CardCreateDto();
        createDto.setCardNumber("1111222233334444");
        createDto.setOwnerId(2L);
        createDto.setExpiryDate(LocalDate.parse("2029-12-31"));

        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden());

        verify(cardService, never()).createCard(any());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/v1/cards/transfer — Успешный перевод пользователем")
    void transferBetweenOwnCards_Success() throws Exception {
        TransferRequestDto transferDto = new TransferRequestDto();
        transferDto.setFromCardId(1L);
        transferDto.setToCardId(2L);
        transferDto.setAmount(new BigDecimal("100.00"));

        when(currentUserService.currentUsername()).thenReturn("user");
        doNothing().when(cardService).transferBetweenOwnCards(eq("user"), any(TransferRequestDto.class));

        mockMvc.perform(post("/api/v1/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDto)))
                .andExpect(status().isOk());
    }
}
