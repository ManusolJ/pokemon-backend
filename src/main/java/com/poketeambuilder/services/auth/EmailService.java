package com.poketeambuilder.services.auth;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    public void sendPasswordResetEmail(String to, String resetUrl) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setFrom(fromAddress);
        message.setSubject("PokéTeam Builder — Password Reset");
        message.setText(
            "You requested a password reset.\n\n" +
            "Click the link below to reset your password:\n" +
            resetUrl + "\n\n" +
            "This link expires in 30 minutes.\n\n" +
            "If you didn't request this, you can safely ignore this email."
        );

        mailSender.send(message);
    }
}