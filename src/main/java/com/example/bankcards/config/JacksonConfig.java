package com.example.bankcards.config;

import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.util.CardNumberSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfig {

    public interface CardResponseDtoMixin {
        @JsonSerialize(using = CardNumberSerializer.class)
        String getCardNumber();
    }

    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();

        objectMapper.addMixIn(CardResponseDto.class, CardResponseDtoMixin.class);

        return objectMapper;
    }
}
