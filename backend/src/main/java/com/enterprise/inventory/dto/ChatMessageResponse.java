package com.enterprise.inventory.dto;

import java.time.Instant;
import java.util.List;

public record ChatMessageResponse(
        String answer,
        String model,
        Instant generatedAt,
        List<String> dataSources
) {
}
