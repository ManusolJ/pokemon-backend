package com.poketeambuilder.services.query;

import org.springframework.stereotype.Service;

import org.springframework.data.jpa.domain.Specification;

import org.springframework.validation.annotation.Validated;

import com.poketeambuilder.dtos.front.item.ItemFilterDto;
import com.poketeambuilder.dtos.front.item.ItemReadDto;

import com.poketeambuilder.entities.Item;

import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.implementation.ItemMapper;

import com.poketeambuilder.repositories.BaseRepository;
import com.poketeambuilder.repositories.ItemRepository;

import com.poketeambuilder.utils.enums.SearchOperation;
import com.poketeambuilder.utils.specification.SpecificationBuilder;

import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
public class ItemQueryService extends AbstractQueryService<Item, Integer, ItemFilterDto, ItemReadDto> {

    private ItemMapper itemMapper;
    private ItemRepository itemRepository;

    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";

    @Override
    protected String getEntityName() {
        return "Item";
    }

    @Override
    protected ReadMapper<Item, ItemReadDto> getMapper() {
        return itemMapper;
    }

    @Override
    protected BaseRepository<Item, Integer> getRepository() {
        return itemRepository;
    }

    @Override
    protected Specification<Item> buildSpecification(ItemFilterDto filter) {
        SpecificationBuilder<Item> builder = new SpecificationBuilder<>();

        if (!filter.hasAnyCriteria()) {
            return builder.build();
        }

        if (filter.getId() != null) {
            builder.with(FIELD_ID, filter.getId(), SearchOperation.EQUAL);
        }

        if (filter.getName() != null && !filter.getName().isEmpty()) {
            builder.with(FIELD_NAME, filter.getName(), SearchOperation.LIKE);
        }

        if (filter.getNameExact() != null && !filter.getNameExact().isEmpty()) {
            builder.with(FIELD_NAME, filter.getNameExact(), SearchOperation.EQUAL);
        }

        return builder.build();
    }
    
}
