package com.poketeambuilder.services.seed;

import java.util.List;

import com.poketeambuilder.dtos.pokeapi.common.PokeApiResource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the item reference de-duplication.
 *
 */
class ItemSeedServiceTest {

    private static PokeApiResource item(String name, int id) {
        return new PokeApiResource(name, "https://pokeapi.co/api/v2/item/" + id + "/");
    }

    @Test
    @DisplayName("Keeps the original entry when the same item is listed under two ids")
    void keepsLowestIdForDuplicateName() {
        List<PokeApiResource> deduplicated = ItemSeedService.deduplicateByName(List.of(
                item("roseli-berry", 723),
                item("roseli-berry", 2279)));

        assertThat(deduplicated).hasSize(1);
        assertThat(deduplicated.getFirst().extractId()).isEqualTo(723);
    }

    @Test
    @DisplayName("Keeps the original entry regardless of which id is listed first")
    void orderOfAppearanceDoesNotDecide() {
        List<PokeApiResource> deduplicated = ItemSeedService.deduplicateByName(List.of(
                item("roseli-berry", 2279),
                item("roseli-berry", 723)));

        assertThat(deduplicated).hasSize(1);
        assertThat(deduplicated.getFirst().extractId()).isEqualTo(723);
    }

    @Test
    @DisplayName("Leaves a list with no duplicates untouched, in order")
    void passesThroughDistinctNames() {
        List<PokeApiResource> input = List.of(
                item("leftovers", 234),
                item("choice-band", 220),
                item("life-orb", 270));

        assertThat(ItemSeedService.deduplicateByName(input))
                .extracting(PokeApiResource::name)
                .containsExactly("leftovers", "choice-band", "life-orb");
    }

    @Test
    @DisplayName("Distinct items are never collapsed, however close their ids")
    void doesNotCollapseDifferentNames() {
        List<PokeApiResource> deduplicated = ItemSeedService.deduplicateByName(List.of(
                item("occa-berry", 211),
                item("passho-berry", 212)));

        assertThat(deduplicated).hasSize(2);
    }

    @Test
    @DisplayName("A reference with an unparseable id loses to one that resolves")
    void prefersAResolvableReference() {
        PokeApiResource unresolvable = new PokeApiResource("roseli-berry", "not-a-url");

        assertThat(ItemSeedService.deduplicateByName(List.of(unresolvable, item("roseli-berry", 723))))
                .singleElement()
                .extracting(PokeApiResource::extractId)
                .isEqualTo(723);

        assertThat(ItemSeedService.deduplicateByName(List.of(item("roseli-berry", 723), unresolvable)))
                .singleElement()
                .extracting(PokeApiResource::extractId)
                .isEqualTo(723);
    }

    @Test
    @DisplayName("An empty reference list is handled")
    void handlesEmptyInput() {
        assertThat(ItemSeedService.deduplicateByName(List.of())).isEmpty();
    }
}
