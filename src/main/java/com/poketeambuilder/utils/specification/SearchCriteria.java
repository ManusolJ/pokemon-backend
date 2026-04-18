package com.poketeambuilder.utils.specification;

import com.poketeambuilder.utils.enums.SearchOperation;

public record SearchCriteria(
    String key,
    Object value,
    Object valueTo,
    SearchOperation operation
) {}
