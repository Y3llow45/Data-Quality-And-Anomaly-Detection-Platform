package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
    @NotBlank @Email String email,
    @NotBlank @Size(max = 30) String username,
    @NotBlank @Size(min = 12, max = 40) String password
) {
}
