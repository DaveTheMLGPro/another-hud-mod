package net.davethemlgpro.client.module.itempickup;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemPickupFilterTest {
	private static final List<String> FILTERED = List.of("minecraft:cobblestone");

	@Test
	void showAllIgnoresTheList() {
		assertTrue(ItemPickupFilter.allows(ItemPickupFilterMode.SHOW_ALL,
			FILTERED, "minecraft:cobblestone"));
	}

	@Test
	void hideListedRejectsOnlyMatchingItemTypes() {
		assertFalse(ItemPickupFilter.allows(ItemPickupFilterMode.HIDE_LISTED,
			FILTERED, "minecraft:cobblestone"));
		assertTrue(ItemPickupFilter.allows(ItemPickupFilterMode.HIDE_LISTED,
			FILTERED, "minecraft:diamond"));
	}

	@Test
	void onlyListedAcceptsOnlyMatchingItemTypes() {
		assertTrue(ItemPickupFilter.allows(ItemPickupFilterMode.ONLY_LISTED,
			FILTERED, "minecraft:cobblestone"));
		assertFalse(ItemPickupFilter.allows(ItemPickupFilterMode.ONLY_LISTED,
			FILTERED, "minecraft:diamond"));
	}
}
