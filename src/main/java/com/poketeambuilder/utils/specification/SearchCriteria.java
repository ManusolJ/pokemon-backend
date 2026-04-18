package com.poketeambuilder.utils.specification;

import com.poketeambuilder.utils.enums.SearchOperation;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchCriteria {

    private String key;

    private Object value;

    private Object valueTo;

    private SearchOperation operation;
}
