package net.davethemlgpro.client.module.itempickup;

import com.google.gson.Gson;
import net.davethemlgpro.client.hud.layout.HudAnchor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
		original.setEntryAnimationSeconds(0.65D);
		original.setPresentation(ItemPickupPresentation.CARDS);
		original.setGrowthDirection(ItemPickupGrowthDirection.LEFT);
		original.setEntryAnimation(ItemPickupEntryAnimation.INSTANT);
		original.setMergeFeedback(ItemPickupMergeFeedback.NONE);
		original.setStableWidth(false);
		original.setUiScale(1.4D);
		original.setRowSpacing(7);
		original.setShowItemIcon(false);
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
		assertEquals(0.65D, restored.getEntryAnimationSeconds());
		assertEquals(ItemPickupPresentation.CARDS, restored.getPresentation());
		assertEquals(ItemPickupGrowthDirection.LEFT, restored.getGrowthDirection());
		assertEquals(ItemPickupEntryAnimation.INSTANT, restored.getEntryAnimation());
		assertEquals(ItemPickupMergeFeedback.NONE, restored.getMergeFeedback());
		assertFalse(restored.isStableWidth());
		assertEquals(1.4D, restored.getUiScale());
		assertEquals(7, restored.getRowSpacing());
		assertFalse(restored.isShowItemIcon());
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
			  "maxVisibleItems": 99,
			  "displayTimeSeconds": -4,
			  "removeDelaySeconds": 99,
			  "fadeDurationSeconds": -2,
			  "mergeWindowSeconds": -3,
			  "entryAnimationSeconds": 99,
			  "presentation": null,
			  "growthDirection": null,
			  "entryAnimation": null,
			  "mergeFeedback": null,
			  "uiScale": 99,
			  "rowSpacing": 99,
			  "style": null,
			  "countFormat": null,
			  "backgroundStyle": null,
			  "removalMode": null
			}
			""", ItemPickupHudConfig.class);
		restored.validate();

		assertEquals(HudAnchor.BOTTOM_RIGHT, restored.getLayout().getAnchor());
		assertEquals(-8, restored.getLayout().getOffsetX());
		assertEquals(-8, restored.getLayout().getOffsetY());
		assertEquals(ItemPickupHudConfig.MAX_VISIBLE_ITEMS, restored.getMaxVisibleItems());
		assertEquals(ItemPickupHudConfig.MIN_DISPLAY_TIME_SECONDS, restored.getDisplayTimeSeconds());
		assertEquals(ItemPickupHudConfig.MAX_REMOVE_DELAY_SECONDS, restored.getRemoveDelaySeconds());
		assertEquals(ItemPickupHudConfig.MIN_FADE_DURATION_SECONDS, restored.getFadeDurationSeconds());
		assertEquals(ItemPickupHudConfig.MIN_MERGE_WINDOW_SECONDS, restored.getMergeWindowSeconds());
		assertEquals(ItemPickupHudConfig.MAX_ENTRY_ANIMATION_SECONDS, restored.getEntryAnimationSeconds());
		assertEquals(ItemPickupPresentation.LIST, restored.getPresentation());
		assertEquals(ItemPickupGrowthDirection.AUTO, restored.getGrowthDirection());
		assertEquals(ItemPickupEntryAnimation.SLIDE, restored.getEntryAnimation());
		assertEquals(ItemPickupMergeFeedback.PULSE, restored.getMergeFeedback());
		assertEquals(ItemPickupHudConfig.MAX_UI_SCALE, restored.getUiScale());
		assertEquals(ItemPickupHudConfig.MAX_ROW_SPACING, restored.getRowSpacing());
		assertTrue(restored.isStableWidth());
		assertTrue(restored.isShowItemIcon());
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
		assertEquals(-8, original.getLayout().getOffsetX());
		assertEquals(-8, original.getLayout().getOffsetY());
	}
}
