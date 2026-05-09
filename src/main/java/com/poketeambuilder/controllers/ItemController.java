package com.poketeambuilder.controllers;

import com.poketeambuilder.dtos.front.item.ItemReadDto;
import com.poketeambuilder.dtos.front.item.ItemSummaryDto;
import com.poketeambuilder.dtos.front.item.ItemFilterDto;

import com.poketeambuilder.services.query.ItemQueryService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;

import org.springframework.data.web.PageableDefault;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/items")
public class ItemController {
    
    private final ItemQueryService itemQueryService;

    @PostMapping("/filter")
    public ResponseEntity<Page<ItemReadDto>> getItems(@PageableDefault(page = 0, size = 20, sort = "id", direction = Direction.DESC) Pageable pageable, @RequestBody ItemFilterDto filter) {
        Page<ItemReadDto> items = itemQueryService.filterEntities(filter, pageable);
        return ResponseEntity.ok(items);
    }

    @PostMapping("summaries")
    public ResponseEntity<Page<ItemSummaryDto>> getItemSummaries(@PageableDefault(page = 0, size = 20, sort = "id", direction = Direction.DESC) Pageable pageable, @RequestBody ItemFilterDto filter) {
        Page<ItemSummaryDto> summaries = itemQueryService.filterItemSummaries(filter, pageable);
        return ResponseEntity.ok(summaries);
    }

    @PostMapping("/id")
    public ResponseEntity<ItemReadDto> getItemById(@RequestBody ItemFilterDto filter) {
        ItemReadDto item = itemQueryService.findById(filter.getId());
        return ResponseEntity.ok(item);
    }

    @PostMapping("/count")
    public ResponseEntity<Long> getItemCount(@RequestBody ItemFilterDto filter) {
        long count = itemQueryService.countFilteredEntities(filter);
        return ResponseEntity.ok(count);
    }
}
