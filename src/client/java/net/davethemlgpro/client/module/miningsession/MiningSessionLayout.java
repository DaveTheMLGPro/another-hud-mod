package net.davethemlgpro.client.module.miningsession;

import net.davethemlgpro.client.hud.HudSize;

public final class MiningSessionLayout {
	public static final int PANEL_PADDING = 6;
	public static final int HEADER_HEIGHT = 20;
	public static final int TIMER_HEIGHT = 18;
	public static final int DIVIDER_HEIGHT = 1;
	public static final int SECTION_HEIGHT = 13;
	public static final int ROW_HEIGHT = 20;
	public static final int VALUE_GOAL_HEIGHT = 24;
	public static final int FOOTER_HEIGHT = 17;
	public static final int ITEM_SIZE = 16;
	public static final int MIN_DETAILED_WIDTH = 154;
	public static final int MIN_COMPACT_WIDTH = 96;

	private MiningSessionLayout() {
	}

	public static HudSize measure(MiningSessionRowStyle style, int contentWidth, int blockRows,
							  int inventoryRows, int goalRows, boolean valueGoal,
							  boolean statisticsFooter, int rowSpacing) {
		int minimumWidth = style == MiningSessionRowStyle.ICON_AMOUNT
			? MIN_COMPACT_WIDTH : MIN_DETAILED_WIDTH;
		int width = Math.max(minimumWidth, contentWidth + PANEL_PADDING * 2);
		int height = PANEL_PADDING + HEADER_HEIGHT + TIMER_HEIGHT + DIVIDER_HEIGHT;
		height += rowsHeight(blockRows, rowSpacing);
		if (inventoryRows > 0) {
			height += DIVIDER_HEIGHT + SECTION_HEIGHT + rowsHeight(inventoryRows, rowSpacing);
		}
		if (goalRows > 0 || valueGoal) {
			height += DIVIDER_HEIGHT + SECTION_HEIGHT;
			height += valueGoal ? VALUE_GOAL_HEIGHT : rowsHeight(goalRows, rowSpacing);
		}
		if (statisticsFooter) {
			height += DIVIDER_HEIGHT + FOOTER_HEIGHT;
		}
		height += PANEL_PADDING;
		return new HudSize(width, height);
	}

	private static int rowsHeight(int rows, int spacing) {
		return rows == 0 ? 0 : rows * ROW_HEIGHT + (rows - 1) * spacing;
	}
}
