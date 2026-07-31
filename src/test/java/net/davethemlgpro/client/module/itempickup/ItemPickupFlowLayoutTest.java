package net.davethemlgpro.client.module.itempickup;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemPickupFlowLayoutTest {
	@Test
	void negativeGrowthKeepsNewestInLastPreviewSlotAndPushesOverflowOut() {
		assertEquals(-1, ItemPickupFlowLayout.slot(0, 4, 3, true));
		assertEquals(2, ItemPickupFlowLayout.slot(3, 4, 3, true));
	}

	@Test
	void positiveGrowthKeepsNewestInFirstPreviewSlotAndPushesOverflowOut() {
		assertEquals(3, ItemPickupFlowLayout.slot(0, 4, 3, false));
		assertEquals(0, ItemPickupFlowLayout.slot(3, 4, 3, false));
	}

	@Test
	void entryOffsetSettlesFromOneWholeSlot() {
		assertEquals(24, ItemPickupFlowLayout.entryOffset(24, 0.0F, true));
		assertEquals(-24, ItemPickupFlowLayout.entryOffset(24, 0.0F, false));
		assertEquals(0, ItemPickupFlowLayout.entryOffset(24, 1.0F, true));
	}

	@Test
	void autoDirectionGrowsAwayFromNearestScreenEdge() {
		assertEquals(false, ItemPickupFlowLayout.growsTowardNegative(4, 64, 240));
		assertEquals(true, ItemPickupFlowLayout.growsTowardNegative(172, 64, 240));
	}
}
