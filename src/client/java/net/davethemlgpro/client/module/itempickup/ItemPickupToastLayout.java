package net.davethemlgpro.client.module.itempickup;

import net.davethemlgpro.client.hud.HudSize;

public final class ItemPickupToastLayout {
	public static final int ITEM_SIZE = 16;
	public static final int ROW_HEIGHT = 22;
	public static final int ROW_GAP = 2;
	public static final int ICON_LEFT = 4;
	public static final int ICON_TOP = 3;
	public static final int TEXT_LEFT = 24;
	public static final int TEXT_TOP = 7;
	public static final int RIGHT_PADDING = 8;
	public static final int NORMAL_MIN_WIDTH = 96;
	public static final int COMPACT_MIN_WIDTH = 44;

	private ItemPickupToastLayout() {
	}

	public static HudSize measure(int rowCount, int widestText) {
		return measure(rowCount, widestText, NORMAL_MIN_WIDTH);
	}

	public static HudSize measure(int rowCount, int widestText, int minimumWidth) {
		return measure(rowCount, widestText, minimumWidth, TEXT_LEFT, ROW_GAP);
	}

	public static HudSize measure(int rowCount, int widestText, int minimumWidth, int textLeft, int rowGap) {
		if (rowCount < 0 || widestText < 0 || minimumWidth < 0 || textLeft < 0 || rowGap < 0) {
			throw new IllegalArgumentException("Toast layout inputs cannot be negative.");
		}
		if (rowCount == 0) {
			return new HudSize(0, 0);
		}
		int width = Math.max(minimumWidth, textLeft + widestText + RIGHT_PADDING);
		int height = stackHeight(rowCount, rowGap);
		return new HudSize(width, height);
	}

	public static int bottomAlignedFirstRowY(int containerHeight, int rowCount) {
		if (containerHeight < 0 || rowCount < 0) {
			throw new IllegalArgumentException("Toast layout inputs cannot be negative.");
		}
		return containerHeight - stackHeight(rowCount);
	}

	public static int rowY(int index) {
		return rowY(index, ROW_GAP);
	}

	public static int rowY(int index, int rowGap) {
		if (index < 0 || rowGap < 0) {
			throw new IllegalArgumentException("Toast index cannot be negative.");
		}
		return index * (ROW_HEIGHT + rowGap);
	}

	private static int stackHeight(int rowCount) {
		return stackHeight(rowCount, ROW_GAP);
	}

	private static int stackHeight(int rowCount, int rowGap) {
		return rowCount == 0 ? 0 : rowCount * ROW_HEIGHT + (rowCount - 1) * rowGap;
	}
}
