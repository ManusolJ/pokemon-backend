package com.poketeambuilder.services.auth;

import org.springframework.stereotype.Component;

import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

/**
 * Tells the previous address when an account's email changes, so an owner whose address was
 * swapped out from under them finds out. Runs after commit; the send itself is asynchronous.
 */
@Component
@RequiredArgsConstructor
public class EmailChangeNotifier {

    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEmailChanged(EmailChangedEvent event) {
        emailService.sendEmailChangedNotice(event.oldEmail(), event.username());
    }
}
