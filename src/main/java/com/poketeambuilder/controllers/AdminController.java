package com.poketeambuilder.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poketeambuilder.dtos.front.admin.seed.SeedLogReadDto;
import com.poketeambuilder.entities.SeedLog;
import com.poketeambuilder.services.command.SeedLogCommandService;
import com.poketeambuilder.services.query.SeedLogQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final SeedLogCommandService seedService;
    private final SeedLogQueryService seedLogQueryService;

    @PostMapping("/seed")
    public ResponseEntity<SeedLogReadDto> triggerSeed(@AuthenticationPrincipal UserDetails user) {
        SeedLog seedLog = seedService.executeSeed(user.getUsername());
        SeedLogReadDto seedLogReadDto = seedLogQueryService.findById(seedLog.getId());
        return new ResponseEntity<>(seedLogReadDto, HttpStatus.OK);
    }
}
