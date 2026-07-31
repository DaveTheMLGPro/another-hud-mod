package net.davethemlgpro.client.module.itempickup;

public final class ItemPickupFlowLayout {
	private ItemPickupFlowLayout() {
	}

	public static int slot(int index, int count, int capacity, boolean towardNegative) {
		if (index < 0 || index >= count || count < 0 || capacity <= 0) {
			throw new IllegalArgumentException("Invalid pickup flow layout input.");
		}
		return towardNegative ? capacity - count + index : count - 1 - index;
	}

	public static int entryOffset(int step, float progress, boolean towardNegative) {
		if (step < 0) {
			throw new IllegalArgumentException("Pickup flow step cannot be negative.");
		}
		float clamped = Math.clamp(progress, 0.0F, 1.0F);
		int remaining = Math.round(step * (1.0F - clamped));
		return towardNegative ? remaining : -remaining;
	}

	public static boolean growsTowardNegative(int position, int size, int screenSize) {
		if (size < 0 || screenSize < 0) {
			throw new IllegalArgumentException("Pickup flow dimensions cannot be negative.");
		}
		return (long) position * 2L + size > screenSize;
	}
}
