package com.example.bankcards.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class CardNumberSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null || value.isBlank()) {
            gen.writeNull();
            return;
        }

        String cleanValue = value.replaceAll("\\s|-", "");

        if (cleanValue.length() >= 4) {
            String lastFour = cleanValue.substring(cleanValue.length() - 4);
            gen.writeString("**** **** **** " + lastFour);
        } else {
            gen.writeString("**** **** **** ****");
        }
    }
}
