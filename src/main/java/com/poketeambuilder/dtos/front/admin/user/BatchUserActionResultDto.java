package com.poketeambuilder.dtos.front.admin.user;

import java.util.List;

/**
 * Per-id outcome of an admin batch action. Each id lands in exactly one list:
 * <ul>
 *   <li>{@code succeeded}: the action was applied.</li>
 *   <li>{@code skipped}: refused on purpose - the id doesn't exist, it is the last admin, or its
 *       identifiers are now held by another account.</li>
 *   <li>{@code failed}: an unexpected error; the server log has the details.</li>
 * </ul>
 */
public record BatchUserActionResultDto(List<Long> succeeded, List<Long> skipped, List<Long> failed) {

    @Override
    public String toString() {
        return String.format("succeeded %s, skipped %s, failed %s", succeeded, skipped, failed);
    }
}
