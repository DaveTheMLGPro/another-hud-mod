package net.davethemlgpro.client.module.armor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArmorHudElementLayoutTest {
	private final ArmorHudModule module = new ArmorHudModule();
	private final ArmorHudConfig config = new ArmorHudConfig();

	@Test
	void groupedModeExposesOneElementUsingGroupedLayout() {
		assertEquals(1, module.elementCount(config));
		assertSame(config.getLayout(), module.elementLayout(config, 0));
		assertThrows(IndexOutOfBoundsException.class, () -> module.elementLayout(config, 1));
	}

	@Test
	void individualModeExposesOneElementPerArmorSlot() {
		config.setLayoutMode(ArmorHudLayoutMode.INDIVIDUAL);

		assertEquals(ArmorHudConfig.ARMOR_SLOT_COUNT, module.elementCount(config));
		for (int slot = 0; slot < ArmorHudConfig.ARMOR_SLOT_COUNT; slot++) {
			assertSame(config.getIndividualLayout(slot), module.elementLayout(config, slot));
		}
		assertThrows(IndexOutOfBoundsException.class,
			() -> module.elementLayout(config, ArmorHudConfig.ARMOR_SLOT_COUNT));
	}

	@Test
	void groupedAndIndividualVisibilityArePreservedIndependently() {
		module.setElementVisible(config, 0, false);
		assertFalse(module.elementVisible(config, 0));

		config.setLayoutMode(ArmorHudLayoutMode.INDIVIDUAL);
		for (int slot = 0; slot < ArmorHudConfig.ARMOR_SLOT_COUNT; slot++) {
			assertTrue(module.elementVisible(config, slot));
		}

		module.setElementVisible(config, 2, false);
		assertFalse(module.elementVisible(config, 2));
		assertTrue(module.elementVisible(config, 0));
		assertTrue(module.elementVisible(config, 1));
		assertTrue(module.elementVisible(config, 3));

		config.setLayoutMode(ArmorHudLayoutMode.GROUPED);
		assertFalse(module.elementVisible(config, 0));
	}
}
