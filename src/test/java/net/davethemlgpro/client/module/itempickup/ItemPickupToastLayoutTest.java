package net.davethemlgpro.client.module.itempickup;

import net.davethemlgpro.client.hud.HudSize;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemPickupToastLayoutTest {
	@Test
	void measuresVerticalToastStack() {
		HudSize size = ItemPickupToastLayout.measure(3, 72);

		assertEquals(104, size.width());
		assertEquals(70, size.height());
		assertEquals(0, ItemPickupToastLayout.rowY(0));
		assertEquals(24, ItemPickupToastLayout.rowY(1));
		assertEquals(48, ItemPickupToastLayout.rowY(2));
	}

	@Test
	void maintainsMinimumWidthAndEmptySize() {
		assertEquals(new HudSize(96, 22), ItemPickupToastLayout.measure(1, 5));
		assertEquals(new HudSize(44, 22), ItemPickupToastLayout.measure(1, 5,
			ItemPickupToastLayout.COMPACT_MIN_WIDTH));
		assertEquals(new HudSize(0, 0), ItemPickupToastLayout.measure(0, 0));
	}

	@Test
	void bottomAlignsPartialStackWithinItsCapacity() {
		int threeRowHeight = ItemPickupToastLayout.measure(3, 0).height();

		assertEquals(48, ItemPickupToastLayout.bottomAlignedFirstRowY(threeRowHeight, 1));
		assertEquals(24, ItemPickupToastLayout.bottomAlignedFirstRowY(threeRowHeight, 2));
		assertEquals(0, ItemPickupToastLayout.bottomAlignedFirstRowY(threeRowHeight, 3));
		assertEquals(-72, ItemPickupToastLayout.bottomAlignedFirstRowY(threeRowHeight, 6));
	}

	@Test
	void rejectsInvalidInputs() {
		assertThrows(IllegalArgumentException.class, () -> ItemPickupToastLayout.measure(-1, 0));
		assertThrows(IllegalArgumentException.class, () -> ItemPickupToastLayout.measure(1, -1));
		assertThrows(IllegalArgumentException.class,
			() -> ItemPickupToastLayout.measure(1, 1, -1));
		assertThrows(IllegalArgumentException.class, () -> ItemPickupToastLayout.rowY(-1));
		assertThrows(IllegalArgumentException.class,
			() -> ItemPickupToastLayout.bottomAlignedFirstRowY(-1, 1));
	}
}
