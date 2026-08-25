package com.poketeambuilder.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Addresses used for outbound mail, bound from {@code app.mail}.
 *
 * <p>Distinct from Spring's own {@code spring.mail} block, which carries the SMTP transport
 * settings. This one is only about who the mail claims to be from and where the contact form
 * lands. Both are validated as addresses, since a malformed value here fails at send time -
 * inside a background thread for the reset flow - rather than at startup.</p>
 *
 * @param from      sender address stamped on every outbound message
 * @param contactTo inbox that receives contact-form submissions
 */
@Validated
@ConfigurationProperties("app.mail")
public record MailProperties(
        @Email @NotBlank String from,
        @Email @NotBlank String contactTo) {
}
