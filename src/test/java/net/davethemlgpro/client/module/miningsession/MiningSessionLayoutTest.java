package net.davethemlgpro.client.module.miningsession;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MiningSessionLayoutTest {
	@Test
	void detailedPanelUsesMinimumWidthAndAccountsForSections() {
		var size = MiningSessionLayout.measure(MiningSessionRowStyle.ICON_NAME_AMOUNT,
			120, 4, 2, 0, false, false, 2);

		assertEquals(MiningSessionLayout.MIN_DETAILED_WIDTH, size.width());
		assertEquals(6 + 20 + 18 + 1 + (4 * 20 + 3 * 2) + 1 + 13 + (2 * 20 + 2) + 6,
			size.height());
	}

	@Test
	void compactPanelCanGrowBeyondItsMinimumWidth() {
		var size = MiningSessionLayout.measure(MiningSessionRowStyle.ICON_AMOUNT,
			110, 1, 0, 0, false, false, 0);

		assertEquals(122, size.width());
		assertEquals(6 + 20 + 18 + 1 + 20 + 6, size.height());
	}

	@Test
	void goalsAndStatisticsAddDedicatedSections() {
		var itemGoalSize = MiningSessionLayout.measure(MiningSessionRowStyle.ICON_NAME_AMOUNT,
			120, 1, 0, 2, false, true, 1);
		var valueGoalSize = MiningSessionLayout.measure(MiningSessionRowStyle.ICON_NAME_AMOUNT,
			120, 1, 0, 0, true, true, 1);

		int baseHeight = 6 + 20 + 18 + 1 + 20 + 6;
		assertEquals(baseHeight + 1 + 13 + 41 + 1 + MiningSessionLayout.FOOTER_HEIGHT,
			itemGoalSize.height());
		assertEquals(baseHeight + 1 + 13 + MiningSessionLayout.VALUE_GOAL_HEIGHT
			+ 1 + MiningSessionLayout.FOOTER_HEIGHT, valueGoalSize.height());
	}
}
