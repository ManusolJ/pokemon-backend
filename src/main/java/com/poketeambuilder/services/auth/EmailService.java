package com.poketeambuilder.services.auth;

import com.poketeambuilder.configuration.MailProperties;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.scheduling.annotation.Async;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

// TODO: Add email templates and support for HTML emails
// TODO: Email verification for new user registration and email change requests

/**
 * Outbound email. The password-reset link, the email-change notice and the contact-form
 * forwarder.
 *
 * <p>Account emails are sent asynchronously: the reset request answers the same way, in the
 * same time, whether or not the address belongs to an account, and an SMTP failure can't turn
 * into a 500 that only known addresses produce. Failures are logged by Spring's async
 * uncaught-exception handler. The contact form stays synchronous so the sender learns when
 * their message didn't go out.</p>
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    /**
     * Sends the one-time password-reset link to the user.
     *
     * @param expirationMinutes the token's real lifetime.
     */
    @Async
    public void sendPasswordResetEmail(String to, String resetUrl, int expirationMinutes) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setFrom(mailProperties.from());
        message.setSubject("PokeTeam Builder — Password Reset");
        message.setText(
            "You requested a password reset.\n\n" +
            "Click the link below to reset your password:\n" +
            resetUrl + "\n\n" +
            "This link expires in " + expirationMinutes + " minutes.\n\n" +
            "If you didn't request this, you can safely ignore this email."
        );

        mailSender.send(message);
    }

    /** Tells the previous address that the account's email was changed. */
    @Async
    public void sendEmailChangedNotice(String oldEmail, String username) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(oldEmail);
        message.setFrom(mailProperties.from());
        message.setSubject("PokeTeam Builder — Your email address was changed");
        message.setText(
            "The email address on the PokeTeam Builder account '" + username + "' was just changed, " +
            "and every signed-in session was ended.\n\n" +
            "If you made this change, there is nothing else to do.\n\n" +
            "If you didn't, someone else has your password. Reach us through the contact form on the site " +
            "so we can restore your account."
        );

        mailSender.send(message);
    }

    /**
     * Forwards a contact-form submission to the configured admin inbox. Line breaks are stripped
     * from the values that end up in headers, so a submission can't smuggle in extra headers
     * such as {@code Bcc:}.
     */
    public void sendContactEmail(String name, String senderEmail, String subject, String message) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(mailProperties.contactTo());
        mail.setFrom(mailProperties.from());
        mail.setReplyTo(singleLine(senderEmail));
        mail.setSubject("PokeTeam Builder — Contact: " + singleLine(subject));
        mail.setText("From: " + singleLine(name) + " <" + singleLine(senderEmail) + ">\n\n" + message);
        mailSender.send(mail);
    }

    private static String singleLine(String value) {
        return value == null ? null : value.replaceAll("[\\r\\n]+", " ");
    }
}
