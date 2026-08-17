package net.davethemlgpro.client.module.containersearch;

import net.davethemlgpro.client.hud.HudBounds;
import net.davethemlgpro.client.hud.HudSize;
import net.davethemlgpro.client.hud.layout.HudLayoutEngine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContainerSearchLayoutTest {
	private static final HudSize PANEL_SIZE = new HudSize(
		ContainerSearchHudModule.PANEL_WIDTH, ContainerSearchHudModule.PANEL_HEIGHT);

	@Test
	void defaultLayoutPlacesPanelBesideCenteredVanillaWidthContainer() {
		ContainerSearchHudConfig config = new ContainerSearchHudConfig();

		HudBounds bounds = HudLayoutEngine.resolve(config.getLayout(), PANEL_SIZE, 854, 480);

		assertEquals(519, bounds.x());
		assertEquals(146, bounds.y());
		assertEquals(ContainerSearchHudModule.PANEL_WIDTH, bounds.width());
		assertEquals(ContainerSearchHudModule.PANEL_HEIGHT, bounds.height());
	}

	@Test
	void liveDragUsesSharedLayoutAndRemainsResolvedAfterward() {
		ContainerSearchHudConfig config = new ContainerSearchHudConfig();
		HudLayoutEngine engine = new HudLayoutEngine();

		HudBounds dragged = engine.applyDrag(config.getLayout(), PANEL_SIZE, 42, 63, 854, 480);
		HudBounds restored = HudLayoutEngine.resolve(config.getLayout(), PANEL_SIZE, 854, 480);

		assertEquals(dragged, restored);
		assertEquals(42, restored.x());
		assertEquals(63, restored.y());
	}

	@Test
	void visibleInputAreaAndControlsHaveSeparateHitboxes() {
		HudBounds panel = new HudBounds(100, 50,
			ContainerSearchHudModule.PANEL_WIDTH, ContainerSearchHudModule.PANEL_HEIGHT);

		assertTrue(ContainerSearchPanelRenderer.inputContains(panel, 109, 76));
		assertTrue(ContainerSearchPanelRenderer.inputContains(panel, 200, 90));
		assertFalse(ContainerSearchPanelRenderer.inputContains(panel, 250, 85));
		assertTrue(ContainerSearchPanelRenderer.clearButtonContains(panel, 250, 85));
		assertTrue(ContainerSearchPanelRenderer.toggleContains(panel, 250, 136));
		assertFalse(ContainerSearchPanelRenderer.toggleContains(panel, 200, 136));
		assertTrue(ContainerSearchPanelRenderer.exactToggleContains(panel, 250, 158));
		assertFalse(ContainerSearchPanelRenderer.exactToggleContains(panel, 200, 158));
	}

	@Test
	void panelBoundsResizeWithConfiguredUiScale() {
		ContainerSearchHudConfig config = new ContainerSearchHudConfig();
		config.setUiScale(1.5D);

		HudSize size = ContainerSearchHudModule.panelSize(config);

		assertEquals(258, size.width());
		assertEquals(186, size.height());
	}

	@Test
	void clearButtonTurnsRedOnHoverAndUsesPressedFeedback() {
		assertEquals(0xFFAAAAAA, ContainerSearchPanelRenderer.clearButtonColor(false, false));
		assertEquals(0xFFFF5555, ContainerSearchPanelRenderer.clearButtonColor(true, false));
		assertEquals(0xFFCC2222, ContainerSearchPanelRenderer.clearButtonColor(true, true));
	}
}
