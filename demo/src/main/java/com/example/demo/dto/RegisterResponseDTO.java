package com.example.demo.dto;

import java.time.LocalDateTime;

public record RegisterResponseDTO(
    LocalDateTime createdAt,
    String token
) {
}
