package com.poketeambuilder.controllers;

import com.poketeambuilder.services.auth.EmailService;
import com.poketeambuilder.dtos.front.mail.ContactRequest;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contact")
public class ContactController {
    private final EmailService emailService;

    @PostMapping
    public ResponseEntity<Void> contact(@RequestBody @Valid ContactRequest request) {
        emailService.sendContactEmail(
            request.name(), request.email(), request.subject(), request.message()
        );
        return ResponseEntity.ok().build();
    }
}
