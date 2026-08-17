package net.davethemlgpro.client.module.containersearch;

import com.google.gson.Gson;
import net.davethemlgpro.client.hud.layout.HudAnchor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContainerSearchHudConfigSerializationTest {
	private final Gson gson = new Gson();

	@Test
	void roundTripsFeatureSettingsColorsAndLayout() {
		ContainerSearchHudConfig original = new ContainerSearchHudConfig();
		original.setVisible(false);
		original.getLayout().setAnchor(HudAnchor.BOTTOM_RIGHT);
		original.getLayout().setOffset(-190, -96);
		original.setUiScale(1.35D);
		original.setDimNonMatches(false);
		original.setExactMatch(true);
		original.setClearOnClose(true);
		original.setHighlightStyle(ContainerSearchHighlightStyle.CORNERS);
		original.setBackgroundColor(0xEE112233);
		original.setTextColor(0xFFABCDEF);
		original.setHighlightColor(0xFF22CC99);
		original.setDimColor(0xAA010203);

		String json = gson.toJson(original);
		ContainerSearchHudConfig restored = gson.fromJson(json, ContainerSearchHudConfig.class);
		restored.validate();

		assertFalse(restored.visible());
		assertEquals(HudAnchor.BOTTOM_RIGHT, restored.getLayout().getAnchor());
		assertEquals(-190, restored.getLayout().getOffsetX());
		assertEquals(-96, restored.getLayout().getOffsetY());
		assertEquals(1.35D, restored.getUiScale());
		assertFalse(restored.isDimNonMatches());
		assertEquals(true, restored.isExactMatch());
		assertEquals(true, restored.isClearOnClose());
		assertEquals(ContainerSearchHighlightStyle.CORNERS, restored.getHighlightStyle());
		assertEquals(0xEE112233, restored.getBackgroundColor());
		assertEquals(0xFFABCDEF, restored.getTextColor());
		assertEquals(0xFF22CC99, restored.getHighlightColor());
		assertEquals(0xAA010203, restored.getDimColor());
	}

	@Test
	void repairsMissingLayoutAndCopiesItIndependently() {
		ContainerSearchHudConfig restored = gson.fromJson("{\"layout\":null}",
			ContainerSearchHudConfig.class);
		restored.validate();

		assertEquals(HudAnchor.CENTER, restored.getLayout().getAnchor());
		assertEquals(178, restored.getLayout().getOffsetX());
		assertEquals(-32, restored.getLayout().getOffsetY());

		ContainerSearchHudConfig copy = restored.copy();
		copy.getLayout().setOffset(12, 34);
		assertNotSame(restored.getLayout(), copy.getLayout());
		assertEquals(178, restored.getLayout().getOffsetX());
	}

	@Test
	void scaleIsClampedAndNonFiniteValuesReturnToDefault() {
		ContainerSearchHudConfig config = new ContainerSearchHudConfig();

		config.setUiScale(99.0D);
		assertEquals(ContainerSearchHudConfig.MAX_UI_SCALE, config.getUiScale());
		config.setUiScale(-99.0D);
		assertEquals(ContainerSearchHudConfig.MIN_UI_SCALE, config.getUiScale());
		config.setUiScale(Double.NaN);
		assertEquals(1.0D, config.getUiScale());
	}
}
