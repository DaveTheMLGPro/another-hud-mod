package net.davethemlgpro.client.module.itempickup;

import com.google.gson.Gson;
import net.davethemlgpro.client.hud.layout.HudAnchor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class ItemPickupHudConfigSerializationTest {
	private final Gson gson = new Gson();

	@Test
	void roundTripsSettingsAndLayout() {
		ItemPickupHudConfig original = new ItemPickupHudConfig();
		original.setEnabled(false);
		original.getLayout().setAnchor(HudAnchor.BOTTOM_RIGHT);
		original.getLayout().setOffset(-14, -28);
		original.setMaxVisibleItems(7);
		original.setDisplayTimeSeconds(8.5D);
		original.setRemoveDelaySeconds(1.2D);
		original.setFadeDurationSeconds(0.8D);
		original.setMergeWindowSeconds(2.4D);
		original.setStyle(ItemPickupHudStyle.COMPACT);
		original.setCountFormat(ItemPickupCountFormat.MULTIPLY);
		original.setBackgroundStyle(ItemPickupBackgroundStyle.UNIFIED_PANEL);
		original.setBackgroundColor(0x99445566);
		original.setTextColor(0xFF12AB34);
		original.setRemovalMode(ItemPickupRemovalMode.FADE_OUT);

		ItemPickupHudConfig restored = gson.fromJson(gson.toJson(original), ItemPickupHudConfig.class);
		restored.validate();

		assertFalse(restored.enabled());
		assertEquals(HudAnchor.BOTTOM_RIGHT, restored.getLayout().getAnchor());
		assertEquals(-14, restored.getLayout().getOffsetX());
		assertEquals(-28, restored.getLayout().getOffsetY());
		assertEquals(7, restored.getMaxVisibleItems());
		assertEquals(8.5D, restored.getDisplayTimeSeconds());
		assertEquals(1.2D, restored.getRemoveDelaySeconds());
		assertEquals(0.8D, restored.getFadeDurationSeconds());
		assertEquals(2.4D, restored.getMergeWindowSeconds());
		assertEquals(ItemPickupHudStyle.COMPACT, restored.getStyle());
		assertEquals(ItemPickupCountFormat.MULTIPLY, restored.getCountFormat());
		assertEquals(ItemPickupBackgroundStyle.UNIFIED_PANEL, restored.getBackgroundStyle());
		assertEquals(0x99445566, restored.getBackgroundColor());
		assertEquals(0xFF12AB34, restored.getTextColor());
		assertEquals(ItemPickupRemovalMode.FADE_OUT, restored.getRemovalMode());
	}

	@Test
	void malformedValuesAreRepaired() {
		ItemPickupHudConfig restored = gson.fromJson("""
			{
			  "layout": null,
			  "maxVisibleToasts": 99,
			  "displayTimeSeconds": -4,
			  "removeDelaySeconds": 99,
			  "fadeDurationSeconds": -2,
			  "mergeWindowSeconds": -3,
			  "style": null,
			  "countFormat": null,
			  "backgroundStyle": null,
			  "removalMode": null
			}
			""", ItemPickupHudConfig.class);
		restored.validate();

		assertEquals(HudAnchor.CENTER_LEFT, restored.getLayout().getAnchor());
		assertEquals(8, restored.getLayout().getOffsetX());
		assertEquals(ItemPickupHudConfig.MAX_VISIBLE_ITEMS, restored.getMaxVisibleItems());
		assertEquals(ItemPickupHudConfig.MIN_DISPLAY_TIME_SECONDS, restored.getDisplayTimeSeconds());
		assertEquals(ItemPickupHudConfig.MAX_REMOVE_DELAY_SECONDS, restored.getRemoveDelaySeconds());
		assertEquals(ItemPickupHudConfig.MIN_FADE_DURATION_SECONDS, restored.getFadeDurationSeconds());
		assertEquals(ItemPickupHudConfig.MIN_MERGE_WINDOW_SECONDS, restored.getMergeWindowSeconds());
		assertEquals(ItemPickupHudStyle.NORMAL, restored.getStyle());
		assertEquals(ItemPickupCountFormat.PLUS, restored.getCountFormat());
		assertEquals(ItemPickupBackgroundStyle.TINTED_ROWS, restored.getBackgroundStyle());
		assertEquals(ItemPickupHudConfig.DEFAULT_BACKGROUND_COLOR, restored.getBackgroundColor());
		assertEquals(ItemPickupHudConfig.DEFAULT_TEXT_COLOR, restored.getTextColor());
		assertEquals(ItemPickupRemovalMode.INSTANT, restored.getRemovalMode());
	}

	@Test
	void copyOwnsIndependentLayout() {
		ItemPickupHudConfig original = new ItemPickupHudConfig();
		ItemPickupHudConfig copy = original.copy();
		copy.getLayout().setOffset(50, 60);

		assertNotSame(original.getLayout(), copy.getLayout());
		assertEquals(8, original.getLayout().getOffsetX());
		assertEquals(0, original.getLayout().getOffsetY());
	}
}
