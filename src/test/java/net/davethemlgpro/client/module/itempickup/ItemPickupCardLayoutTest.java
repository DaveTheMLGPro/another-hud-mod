package net.davethemlgpro.client.module.itempickup;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemPickupCardLayoutTest {
	@Test
	void measuresThreeCardPreview() {
		assertEquals(ItemPickupCardLayout.CARD_WIDTH * 3 + ItemPickupCardLayout.CARD_GAP * 2,
			ItemPickupCardLayout.measure(3).width());
		assertEquals(ItemPickupCardLayout.CARD_HEIGHT, ItemPickupCardLayout.measure(3).height());
	}

	@Test
	void laysCardsOutAtFixedIntervals() {
		assertEquals(0, ItemPickupCardLayout.cardX(0));
		assertEquals(ItemPickupCardLayout.CARD_WIDTH + ItemPickupCardLayout.CARD_GAP,
			ItemPickupCardLayout.cardX(1));
		assertThrows(IllegalArgumentException.class, () -> ItemPickupCardLayout.measure(-1));
	}

	@Test
	void supportsCustomCardSpacing() {
		assertEquals(ItemPickupCardLayout.CARD_WIDTH * 3 + 16,
			ItemPickupCardLayout.measure(3, 8).width());
		assertEquals(ItemPickupCardLayout.CARD_WIDTH + 8,
			ItemPickupCardLayout.cardX(1, 8));
	}
}
