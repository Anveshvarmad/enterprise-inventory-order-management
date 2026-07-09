package com.enterprise.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatMessageRequest(

        @NotBlank(message = "Message is required")
        @Size(max = 1000, message = "Message cannot exceed 1000 characters")
        String message
) {
}
