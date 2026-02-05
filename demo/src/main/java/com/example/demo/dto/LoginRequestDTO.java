package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDTO(
        @NotBlank @Size(min = 6, max = 254) String usernameOrEmail,
        @NotBlank @Size(min = 12, max = 40) String password
) {
}
