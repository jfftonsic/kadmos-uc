package com.example.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;

@Slf4j
public class MoneySerializer extends JsonSerializer<BigDecimal> {
    private DecimalFormat moneyFormat = new DecimalFormat("##.#");

    @Override
    public void serialize(BigDecimal amount, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
        final var formatted = moneyFormat.format(amount);
        log.info("Serializing amount={} to formatted={}", amount, formatted);
        jsonGenerator.writeNumber(formatted);
    }
}
