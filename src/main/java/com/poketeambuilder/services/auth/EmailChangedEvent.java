package com.poketeambuilder.services.auth;

/**
 * Published when an account's email address changes. Handled after commit, so the notice to
 * the old address only goes out once the change is real.
 */
public record EmailChangedEvent(String username, String oldEmail) {
}
