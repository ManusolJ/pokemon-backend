package com.poketeambuilder.dtos;

import java.util.List;

import com.poketeambuilder.dtos.front.team.details.TeamCreateDto;
import com.poketeambuilder.dtos.front.team.details.TeamUpdateDto;
import com.poketeambuilder.dtos.front.team.roster.TeamPokemonCreateDto;

import com.poketeambuilder.infrastructure.validation.annotations.ValidEvSpread;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.springframework.test.util.ReflectionTestUtils;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Bean Validation on the team payloads.
 *
 * <p>The roster bound is the load-bearing one: {@code @Size(min = 1)} passes on a null list, so
 * the create and update payloads only agree if both also carry {@code @NotNull}. The update
 * payload did not, and the service dereferences the list straight away — a {@code PUT} that
 * omitted the roster wiped it and then threw an unmapped NPE.</p>
 */
class TeamPayloadValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void openValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    @Test
    @DisplayName("An update with no roster is rejected, not passed through to be dereferenced")
    void updateRequiresARoster() {
        TeamUpdateDto dto = new TeamUpdateDto();
        ReflectionTestUtils.setField(dto, "name", "Kanto");
        ReflectionTestUtils.setField(dto, "pokemon", null);

        assertThat(violationPaths(dto)).contains("pokemon");
    }

    @Test
    @DisplayName("A create with no roster is rejected the same way")
    void createRequiresARoster() {
        TeamCreateDto dto = new TeamCreateDto();
        ReflectionTestUtils.setField(dto, "name", "Kanto");
        ReflectionTestUtils.setField(dto, "pokemon", null);

        assertThat(violationPaths(dto)).contains("pokemon");
    }

    @Test
    @DisplayName("A roster of one to six members is accepted; seven is not")
    void rosterSizeIsBounded() {
        TeamUpdateDto ok = update(rosterOf(6));
        TeamUpdateDto tooBig = update(rosterOf(7));

        assertThat(violationPaths(ok)).doesNotContain("pokemon");
        assertThat(violationPaths(tooBig)).contains("pokemon");
    }

    @Test
    @DisplayName("The EV total cap is enforced from a single shared constant")
    void evTotalCapIsEnforced() {
        TeamPokemonCreateDto atCap = member();
        spendEvs(atCap, 252, 252, ValidEvSpread.MAX_TOTAL_EVS - 504);

        TeamPokemonCreateDto overCap = member();
        spendEvs(overCap, 252, 252, 252);

        assertThat(validator.validate(atCap)).isEmpty();
        assertThat(validator.validate(overCap)).isNotEmpty();
    }

    private void spendEvs(TeamPokemonCreateDto dto, int hp, int atk, int def) {
        ReflectionTestUtils.setField(dto, "evHp", hp);
        ReflectionTestUtils.setField(dto, "evAtk", atk);
        ReflectionTestUtils.setField(dto, "evDef", def);
    }

    private TeamUpdateDto update(List<TeamPokemonCreateDto> roster) {
        TeamUpdateDto dto = new TeamUpdateDto();
        ReflectionTestUtils.setField(dto, "name", "Kanto");
        ReflectionTestUtils.setField(dto, "pokemon", roster);
        return dto;
    }

    private List<TeamPokemonCreateDto> rosterOf(int size) {
        return java.util.stream.IntStream.range(0, size).mapToObj(i -> member()).toList();
    }

    private TeamPokemonCreateDto member() {
        TeamPokemonCreateDto dto = new TeamPokemonCreateDto();
        ReflectionTestUtils.setField(dto, "pokemonId", 25);
        ReflectionTestUtils.setField(dto, "abilityId", 9);
        ReflectionTestUtils.setField(dto, "moveIds", List.of(85));
        return dto;
    }

    private List<String> violationPaths(Object dto) {
        return validator.validate(dto).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .toList();
    }
}
