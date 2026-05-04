package com.poketeambuilder.services.query;

import com.poketeambuilder.entities.AppUser;

import com.poketeambuilder.dtos.front.user.UserReadDto;
import com.poketeambuilder.dtos.front.user.UserFilterDto;
import com.poketeambuilder.dtos.front.user.UserSummaryDto;

import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.implementation.UserMapper;

import com.poketeambuilder.repositories.BaseRepository;
import com.poketeambuilder.repositories.AppUserRepository;

import com.poketeambuilder.utils.enums.UserRole;
import com.poketeambuilder.utils.enums.SearchOperation;
import com.poketeambuilder.utils.specification.SpecificationBuilder;

import org.springframework.stereotype.Service;

import org.springframework.cache.CacheManager;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Service
@Validated
public class UserQueryService extends AbstractQueryService<AppUser, Long, UserFilterDto, UserReadDto> {

    private final UserMapper userMapper;
    private final AppUserRepository userRepository;

    public UserQueryService(CacheManager cacheManager, UserMapper userMapper, AppUserRepository userRepository) {
        super(cacheManager);
        this.userMapper = userMapper;
        this.userRepository = userRepository;
    }

    private static final String FIELD_ID = "id";
    private static final String FIELD_ROLE = "role";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_ENABLED = "enabled";
    private static final String FIELD_USERNAME = "username";

    @Override
    protected String getEntityName() {
        return "User";
    }

    @Override
    protected String getCacheName() {
        return null;
    }

    @Override
    protected ReadMapper<AppUser, UserReadDto> getMapper() {
        return userMapper;
    }

    @Override
    protected BaseRepository<AppUser, Long> getRepository() {
        return userRepository;
    }

    public Page<UserSummaryDto> filterSummaries(@Valid @NotNull UserFilterDto filter, @NotNull Pageable pageable) {
        return filterAndMap(filter, pageable, userMapper::toSummaryDto);
    }

    @Override
    protected Specification<AppUser> buildSpecification(@NotNull UserFilterDto filter) {
        SpecificationBuilder<AppUser> builder = new SpecificationBuilder<>();

        if (!filter.hasAnyCriteria()) {
            return builder.build();
        }

        if (filter.getId() != null) {
            builder.with(FIELD_ID, filter.getId(), SearchOperation.EQUAL);
        }
        if (filter.getUsername() != null && !filter.getUsername().isBlank()) {
            builder.with(FIELD_USERNAME, filter.getUsername(), SearchOperation.LIKE);
        }
        if (filter.getUsernameExact() != null && !filter.getUsernameExact().isBlank()) {
            builder.with(FIELD_USERNAME, filter.getUsernameExact(), SearchOperation.EQUAL);
        }
        if (filter.getEmail() != null && !filter.getEmail().isBlank()) {
            builder.with(FIELD_EMAIL, filter.getEmail(), SearchOperation.LIKE);
        }
        if (filter.getRole() != null && !filter.getRole().isBlank()) {
            builder.with(FIELD_ROLE, UserRole.fromValue(filter.getRole()), SearchOperation.EQUAL);
        }
        if (filter.getEnabled() != null) {
            builder.with(FIELD_ENABLED, filter.getEnabled(), SearchOperation.EQUAL);
        }

        return builder.build();
    }
}