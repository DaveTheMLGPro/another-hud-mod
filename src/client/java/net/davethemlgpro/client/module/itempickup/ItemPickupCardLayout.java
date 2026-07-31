package net.davethemlgpro.client.module.itempickup;

import net.davethemlgpro.client.hud.HudSize;

public final class ItemPickupCardLayout {
	public static final int CARD_WIDTH = 48;
	public static final int CARD_HEIGHT = 40;
	public static final int CARD_GAP = 3;
	public static final int ICON_TOP = 4;
	public static final int TEXT_TOP = 27;

	private ItemPickupCardLayout() {
	}

	public static HudSize measure(int cardCount) {
		return measure(cardCount, CARD_GAP);
	}

	public static HudSize measure(int cardCount, int spacing) {
		if (cardCount < 0 || spacing < 0) {
			throw new IllegalArgumentException("Card count cannot be negative.");
		}
		return cardCount == 0 ? new HudSize(0, 0)
			: new HudSize(cardCount * CARD_WIDTH + (cardCount - 1) * spacing, CARD_HEIGHT);
	}

	public static int cardX(int slot) {
		return cardX(slot, CARD_GAP);
	}

	public static int cardX(int slot, int spacing) {
		if (spacing < 0) {
			throw new IllegalArgumentException("Card spacing cannot be negative.");
		}
		return slot * (CARD_WIDTH + spacing);
	}
}
