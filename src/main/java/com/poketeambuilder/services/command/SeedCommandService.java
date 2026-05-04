package com.poketeambuilder.services.command;

import org.springframework.stereotype.Service;

import com.poketeambuilder.services.seed.TypeSeedService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeedCommandService {
    
    private final TypeSeedService typeSeedService;

    public void seed() {
        typeSeedService.seed();
    }
}
