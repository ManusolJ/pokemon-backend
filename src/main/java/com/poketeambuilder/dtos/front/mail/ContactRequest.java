package com.poketeambuilder.dtos.front.mail;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ContactRequest(
    @NotBlank String name,
    @Email @NotBlank String email,
    @NotBlank String subject,
    @NotBlank @Size(min = 10) String message
) {}
