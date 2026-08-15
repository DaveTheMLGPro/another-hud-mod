package net.davethemlgpro.client.module.armor;

import com.google.gson.Gson;
import net.davethemlgpro.client.hud.layout.HudAnchor;
import net.davethemlgpro.client.hud.layout.ModuleLayout;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArmorHudConfigSerializationTest {
	private final Gson gson = new Gson();

	@Test
	void roundTripsIndividualLayoutModeAndSlotPositions() {
		ArmorHudConfig original = new ArmorHudConfig();
		original.setLayoutMode(ArmorHudLayoutMode.INDIVIDUAL);
		original.getLayout().setOffset(12, 34);
		original.getIndividualLayout(0).setAnchor(HudAnchor.BOTTOM_LEFT);
		original.getIndividualLayout(0).setOffset(17, -23);
		original.getIndividualLayout(3).setAnchor(HudAnchor.TOP_CENTER);
		original.getIndividualLayout(3).setOffset(-9, 41);
		original.setIndividualSlotVisible(1, false);
		original.setIndividualSlotVisible(3, false);
		original.setLowDurabilityThresholdPercent(37);
		original.setDurabilityWarningSoundEnabled(true);
		original.setDurabilityWarningSoundThresholdPercent(13);

		ArmorHudConfig restored = gson.fromJson(gson.toJson(original), ArmorHudConfig.class);
		restored.validate();

		assertEquals(ArmorHudLayoutMode.INDIVIDUAL, restored.getLayoutMode());
		assertLayout(restored.getLayout(), HudAnchor.CENTER_RIGHT, 12, 34);
		assertLayout(restored.getIndividualLayout(0), HudAnchor.BOTTOM_LEFT, 17, -23);
		assertLayout(restored.getIndividualLayout(3), HudAnchor.TOP_CENTER, -9, 41);
		assertFalse(restored.isIndividualSlotVisible(1));
		assertFalse(restored.isIndividualSlotVisible(3));
		assertEquals(37, restored.getLowDurabilityThresholdPercent());
		assertTrue(restored.isDurabilityWarningSoundEnabled());
		assertEquals(13, restored.getDurabilityWarningSoundThresholdPercent());
	}

	@Test
	void soundThresholdIsValidatedIndependently() {
		ArmorHudConfig restored = gson.fromJson("""
			{
			  "lowDurabilityThresholdPercent": 64,
			  "durabilityWarningSoundThresholdPercent": 0
			}
			""", ArmorHudConfig.class);
		restored.validate();

		assertEquals(64, restored.getLowDurabilityThresholdPercent());
		assertEquals(ArmorHudConfig.MIN_LOW_DURABILITY_THRESHOLD_PERCENT,
			restored.getDurabilityWarningSoundThresholdPercent());
	}

	@Test
	void missingIndividualFieldsUseGroupedDefaults() {
		ArmorHudConfig restored = gson.fromJson("{}", ArmorHudConfig.class);
		restored.validate();

		assertEquals(ArmorHudLayoutMode.GROUPED, restored.getLayoutMode());
		assertLayout(restored.getIndividualLayout(0), HudAnchor.CENTER_RIGHT, -8, -33);
		assertLayout(restored.getIndividualLayout(1), HudAnchor.CENTER_RIGHT, -8, -11);
		assertLayout(restored.getIndividualLayout(2), HudAnchor.CENTER_RIGHT, -8, 11);
		assertLayout(restored.getIndividualLayout(3), HudAnchor.CENTER_RIGHT, -8, 33);
		for (int slot = 0; slot < ArmorHudConfig.ARMOR_SLOT_COUNT; slot++) {
			assertTrue(restored.isIndividualSlotVisible(slot));
		}
		assertFalse(restored.isDurabilityWarningSoundEnabled());
		assertEquals(10, restored.getDurabilityWarningSoundThresholdPercent());
	}

	@Test
	void malformedIndividualLayoutArrayIsRepaired() {
		ArmorHudConfig restored = gson.fromJson("""
			{
			  "layoutMode": "INDIVIDUAL",
			  "individualLayouts": [
			    {"anchor": "BOTTOM_RIGHT", "offsetX": -20, "offsetY": -30},
			    null
			  ],
			  "individualSlotVisible": [false]
			}
			""", ArmorHudConfig.class);
		restored.validate();

		assertLayout(restored.getIndividualLayout(0), HudAnchor.BOTTOM_RIGHT, -20, -30);
		assertLayout(restored.getIndividualLayout(1), HudAnchor.CENTER_RIGHT, -8, -11);
		assertLayout(restored.getIndividualLayout(3), HudAnchor.CENTER_RIGHT, -8, 33);
		assertFalse(restored.isIndividualSlotVisible(0));
		assertTrue(restored.isIndividualSlotVisible(1));
		assertTrue(restored.isIndividualSlotVisible(3));
	}

	@Test
	void copyOwnsIndependentGroupedAndSlotLayouts() {
		ArmorHudConfig original = new ArmorHudConfig();
		ArmorHudConfig copy = original.copy();

		copy.getLayout().setOffset(1, 2);
		copy.getIndividualLayout(0).setOffset(3, 4);
		copy.setIndividualSlotVisible(0, false);

		assertNotSame(original.getLayout(), copy.getLayout());
		assertNotSame(original.getIndividualLayout(0), copy.getIndividualLayout(0));
		assertLayout(original.getLayout(), HudAnchor.CENTER_RIGHT, -8, 0);
		assertLayout(original.getIndividualLayout(0), HudAnchor.CENTER_RIGHT, -8, -33);
		assertTrue(original.isIndividualSlotVisible(0));
	}

	private static void assertLayout(ModuleLayout layout, HudAnchor anchor, int offsetX, int offsetY) {
		assertEquals(anchor, layout.getAnchor());
		assertEquals(offsetX, layout.getOffsetX());
		assertEquals(offsetY, layout.getOffsetY());
	}
}
