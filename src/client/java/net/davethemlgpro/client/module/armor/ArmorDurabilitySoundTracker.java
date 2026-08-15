package net.davethemlgpro.client.module.armor;

final class ArmorDurabilitySoundTracker {
	private final boolean[] belowThreshold;
	private boolean initialized;

	ArmorDurabilitySoundTracker(int slotCount) {
		if (slotCount <= 0) {
			throw new IllegalArgumentException("Armor sound tracker needs at least one slot.");
		}
		belowThreshold = new boolean[slotCount];
	}

	boolean update(boolean[] currentBelowThreshold) {
		if (currentBelowThreshold.length != belowThreshold.length) {
			throw new IllegalArgumentException("Unexpected armor slot count.");
		}
		boolean crossedThreshold = false;
		for (int slot = 0; slot < belowThreshold.length; slot++) {
			crossedThreshold |= initialized && currentBelowThreshold[slot] && !belowThreshold[slot];
			belowThreshold[slot] = currentBelowThreshold[slot];
		}
		initialized = true;
		return crossedThreshold;
	}

	void reset() {
		initialized = false;
		java.util.Arrays.fill(belowThreshold, false);
	}
}
