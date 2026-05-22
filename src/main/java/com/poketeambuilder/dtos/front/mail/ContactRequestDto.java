package com.poketeambuilder.dtos.front.mail;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Payload for the public contact form. Bounds on every field are intentional to keep abuse vectors small.
 */
public record ContactRequestDto(
    @NotBlank @Size(min = 2, max = 100) String name,
    @Email @NotBlank @Size(max = 255) String email,
    @NotBlank @Size(min = 2, max = 150) String subject,
    @NotBlank @Size(min = 10, max = 2000) String message
) {}
