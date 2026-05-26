package com.poketeambuilder.services.auth;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

// TODO: Add email templates and support for HTML emails
// TODO: Email verification for new user registration and email change requests
// TODO: Implement asynchronous email sending to improve performance and user experience

/**
 * Outbound email. Currently the password-reset link and the contact-form forwarder.
 * Synchronous SMTP today; the TODOs above track the planned async / templated rewrite.
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.mail.contact-to}")
    private String contactToAddress;

    /** Sends the one-time password-reset link to the user. */
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

    /** Forwards a contact-form submission to the configured admin inbox. */
    public void sendContactEmail(String name, String senderEmail, String subject, String message) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(contactToAddress);
        mail.setFrom(fromAddress);
        mail.setReplyTo(senderEmail);
        mail.setSubject("PokéTeam Builder — Contact: " + subject);
        mail.setText("From: " + name + " <" + senderEmail + ">\n\n" + message);
        mailSender.send(mail);
    }
}
