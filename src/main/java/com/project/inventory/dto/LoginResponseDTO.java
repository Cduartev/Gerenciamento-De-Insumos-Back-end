package com.project.inventory.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginResponseDTO(
        String token,
        String name,
        String email,
        String role
) {}
