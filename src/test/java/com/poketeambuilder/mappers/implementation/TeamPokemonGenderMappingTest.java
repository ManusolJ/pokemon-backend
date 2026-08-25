package com.poketeambuilder.mappers.implementation;

import com.poketeambuilder.dtos.front.team.roster.TeamPokemonCreateDto;
import com.poketeambuilder.dtos.front.team.roster.TeamPokemonReadDto;

import com.poketeambuilder.entities.TeamPokemon;

import com.poketeambuilder.utils.enums.PokemonGender;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import org.springframework.test.util.ReflectionTestUtils;

import jakarta.validation.constraints.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The gender field of a roster slot, in both directions.
 *
 * <p>The write side accepts whatever the DTO pattern advertises and the read side emits the enum
 * constant name, so the two only agree as long as the pattern and the enum stay in step. They did
 * not: the pattern offered {@code GENDERLESS}, the enum called that constant {@code NONE}, and
 * MapStruct resolves the incoming string with {@code Enum.valueOf} — so every genderless Pokemon
 * failed with an unmapped {@link IllegalArgumentException}, which the error handler could only
 * report as a 500. These tests pin the agreement rather than the one value that was broken.</p>
 */
class TeamPokemonGenderMappingTest {

    private final TeamPokemonMapper mapper = new TeamPokemonMapperImpl();

    @ParameterizedTest
    @EnumSource(PokemonGender.class)
    @DisplayName("Every gender the DTO advertises is accepted by the mapper")
    void everyAdvertisedGenderMaps(PokemonGender gender) {
        TeamPokemon entity = mapper.toEntity(dtoWithGender(gender.name()));

        assertThat(entity.getGender()).isEqualTo(gender);
    }

    @ParameterizedTest
    @EnumSource(PokemonGender.class)
    @DisplayName("What a read returns is what a write accepts, so a team round-trips")
    void readOutputIsValidWriteInput(PokemonGender gender) {
        TeamPokemonReadDto read = mapper.toReadDto(TeamPokemon.builder().gender(gender).build());

        assertThat(read.gender()).isNotNull();
        assertThat(mapper.toEntity(dtoWithGender(read.gender())).getGender()).isEqualTo(gender);
    }

    @ParameterizedTest
    @EnumSource(PokemonGender.class)
    @DisplayName("The DTO validation pattern admits every constant the enum defines")
    void validationPatternCoversTheWholeEnum(PokemonGender gender) throws NoSuchFieldException {
        Pattern constraint = TeamPokemonCreateDto.class
                .getDeclaredField("gender")
                .getAnnotation(Pattern.class);

        assertThat(constraint).isNotNull();
        assertThat(java.util.regex.Pattern.compile(constraint.regexp()).matcher(gender.name()).matches())
                .as("%s is rejected by the DTO pattern %s", gender.name(), constraint.regexp())
                .isTrue();
    }

    @Test
    @DisplayName("A genderless Pokemon still stores the lowercase value the column has always held")
    void genderlessKeepsItsStorageValue() {
        assertThat(PokemonGender.GENDERLESS.getValue()).isEqualTo("none");
        assertThat(PokemonGender.fromValue("none")).isEqualTo(PokemonGender.GENDERLESS);
        assertThat(PokemonGender.fromValue("GENDERLESS")).isEqualTo(PokemonGender.GENDERLESS);
    }

    @Test
    @DisplayName("An omitted gender stays null rather than defaulting to a value")
    void absentGenderIsLeftUnset() {
        assertThat(mapper.toEntity(dtoWithGender(null)).getGender()).isNull();
    }

    private TeamPokemonCreateDto dtoWithGender(String gender) {
        TeamPokemonCreateDto dto = new TeamPokemonCreateDto();
        ReflectionTestUtils.setField(dto, "gender", gender);
        return dto;
    }
}
