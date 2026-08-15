package net.davethemlgpro.client.module.armor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArmorDurabilitySoundTrackerTest {
	@Test
	void alertsOncePerThresholdCrossingAndRearmsAboveThreshold() {
		ArmorDurabilitySoundTracker tracker = new ArmorDurabilitySoundTracker(4);

		assertFalse(tracker.update(states(false, false, false, false)));
		assertTrue(tracker.update(states(false, true, false, false)));
		assertFalse(tracker.update(states(false, true, false, false)));
		assertFalse(tracker.update(states(false, false, false, false)));
		assertTrue(tracker.update(states(false, true, false, false)));
	}

	@Test
	void initializationAndResetDoNotAlertForAlreadyLowArmor() {
		ArmorDurabilitySoundTracker tracker = new ArmorDurabilitySoundTracker(4);

		assertFalse(tracker.update(states(true, false, false, false)));
		tracker.reset();
		assertFalse(tracker.update(states(true, false, false, false)));
	}

	@Test
	void validatesSlotCounts() {
		assertThrows(IllegalArgumentException.class, () -> new ArmorDurabilitySoundTracker(0));
		ArmorDurabilitySoundTracker tracker = new ArmorDurabilitySoundTracker(4);
		assertThrows(IllegalArgumentException.class, () -> tracker.update(new boolean[3]));
	}

	private static boolean[] states(boolean helmet, boolean chestplate, boolean leggings, boolean boots) {
		return new boolean[] {helmet, chestplate, leggings, boots};
	}
}
