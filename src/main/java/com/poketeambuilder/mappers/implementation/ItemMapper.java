package com.poketeambuilder.mappers.implementation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.poketeambuilder.entities.Item;

import com.poketeambuilder.dtos.front.item.ItemReadDto;
import com.poketeambuilder.dtos.front.item.ItemSummaryDto;

import com.poketeambuilder.dtos.pokeapi.item.ItemApiDto;

import com.poketeambuilder.mappers.common.ApiMapper;
import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.common.SummaryMapper;
import com.poketeambuilder.mappers.common.MapperConfiguration;

import com.poketeambuilder.mappers.helpers.resource.ItemIngestionHelper;

@Mapper(config = MapperConfiguration.class, uses = { ItemIngestionHelper.class })
public interface ItemMapper extends ReadMapper<Item, ItemReadDto>, ApiMapper<Item, ItemApiDto>, SummaryMapper<Item, ItemSummaryDto> {
    
    @Override
    ItemReadDto toReadDto(Item entity);
    
    @Override
    ItemSummaryDto toSummaryDto(Item entity);

    @Override
    @Mapping(target = "category", source = "category", qualifiedByName = "extractItemCategory")
    @Mapping(target = "spriteUrl", source = "sprites", qualifiedByName = "extractItemSpriteUrl")
    @Mapping(target = "effect", source = "effectEntries", qualifiedByName = "extractItemEffect")
    @Mapping(target = "description", source = "flavorTextEntries", qualifiedByName = "extractItemDescription")
    Item toEntity(ItemApiDto apiDto);
}
