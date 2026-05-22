package com.poketeambuilder.dtos.front.admin.user;

import java.util.List;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;

/**
 * Bulk-action payload for admin endpoints (batch enable/disable, batch delete…). The cap of
 * 100 keeps a single request from blocking the worker thread on a huge sweep.
 */
@Getter
public class BatchUserActionDto {

    @NotNull
    @Size(min = 1, max = 100, message = "Batch must contain between 1 and 100 ids")
    private List<Long> ids;
}
