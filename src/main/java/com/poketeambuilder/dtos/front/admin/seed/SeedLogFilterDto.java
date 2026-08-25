package com.poketeambuilder.dtos.front.admin.seed;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import java.time.Instant;

import jakarta.validation.constraints.Pattern;

import lombok.Getter;

/**
 * Filter payload for the admin seed-log listing.
 */
@Getter
public class SeedLogFilterDto implements FilterDtoInterface {

    private Long id;

    /** Matched case-insensitively against {@link com.poketeambuilder.utils.enums.SeedStatus}. */
    @Pattern(regexp = "Running|Completed|Failed|Unknown", flags = Pattern.Flag.CASE_INSENSITIVE,
             message = "Status must be one of Running, Completed, Failed, Unknown")
    private String status;

    private String triggeredBy;

    private String triggeredByExact;

    private Instant dateFrom;

    private Instant dateTo;
}
