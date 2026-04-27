package com.poketeambuilder.mappers.implementation;

import com.poketeambuilder.entities.Item;

import com.poketeambuilder.dtos.front.item.ItemReadDto;
import com.poketeambuilder.dtos.front.item.ItemSummaryDto;

import com.poketeambuilder.dtos.pokeapi.item.ItemApiDto;

import com.poketeambuilder.mappers.common.ApiMapper;
import com.poketeambuilder.mappers.common.ReadMapper;
import com.poketeambuilder.mappers.common.SummaryMapper;
import com.poketeambuilder.mappers.common.MapperConfiguration;

import com.poketeambuilder.mappers.helpers.shared.TextExtractor;
import com.poketeambuilder.mappers.helpers.resource.ItemIngestionHelper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class, uses = { ItemIngestionHelper.class, TextExtractor.class })
public interface ItemMapper extends ReadMapper<Item, ItemReadDto>, ApiMapper<Item, ItemApiDto>, SummaryMapper<Item, ItemSummaryDto> {
    
    @Override
    ItemReadDto toReadDto(Item entity);
    
    @Override
    ItemSummaryDto toSummaryDto(Item entity);

    @Override
    @Mapping(target = "effect", source = "effectEntries", qualifiedByName = "extractEffect")
    @Mapping(target = "category", source = "category", qualifiedByName = "extractItemCategory")
    @Mapping(target = "spriteUrl", source = "sprites", qualifiedByName = "extractItemSpriteUrl")
    @Mapping(target = "shortEffect", source = "effectEntries", qualifiedByName = "extractShortEffect")
    @Mapping(target = "flavorText", source = "flavorTextEntries", qualifiedByName = "extractItemFlavorText")
    Item toEntity(ItemApiDto apiDto);
}
