package com.poketeambuilder.dtos.front.user;

import com.poketeambuilder.interfaces.FilterDtoInterface;

import lombok.Getter;

@Getter
public class UserFilterDto implements FilterDtoInterface {

    private Long id;

    private String name;

    private String nameExact;

    private String email;

    private String role;
    
    private Boolean enabled;

    @Override
    public boolean hasAnyCriteria() {
        return id != null
                || name != null
                || nameExact != null
                || email != null
                || role != null
                || enabled != null;
    }
}