package net.davethemlgpro.client.hud.layout;

import net.davethemlgpro.client.hud.HudBounds;
import net.davethemlgpro.client.hud.HudSize;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HudLayoutEngineTest {
	private final HudLayoutEngine engine = new HudLayoutEngine();

	@Test
	void snapsToNearestGridLine() {
		assertEquals(10, HudLayoutEngine.snapToGrid(12, 5));
		assertEquals(15, HudLayoutEngine.snapToGrid(13, 5));
		assertEquals(20, HudLayoutEngine.snapToGrid(20, 5));
	}

	@Test
	void rejectsNonPositiveGridSpacing() {
		assertThrows(IllegalArgumentException.class, () -> HudLayoutEngine.snapToGrid(10, 0));
		assertThrows(IllegalArgumentException.class, () -> HudLayoutEngine.snapToGrid(10, -5));
	}

	@Test
	void appliesSnappedPositionAsAnchorRelativeOffset() {
		ModuleLayout layout = new ModuleLayout(HudAnchor.CENTER, 0, 0);

		HudBounds bounds = engine.applySnappedDragOffset(layout, 20, 10,
			73, 42, 200, 100, 5);

		assertEquals(new HudBounds(75, 40, 20, 10), bounds);
		assertEquals(-15, layout.getOffsetX());
		assertEquals(-5, layout.getOffsetY());
	}

	@Test
	void clampsSnappedBoundsToScreenEdges() {
		ModuleLayout layout = new ModuleLayout(HudAnchor.TOP_LEFT, 0, 0);

		HudBounds bounds = engine.applySnappedDragOffset(layout, 18, 12,
			99, -4, 100, 60, 5);

		assertEquals(new HudBounds(82, 0, 18, 12), bounds);
		assertEquals(82, layout.getOffsetX());
		assertEquals(0, layout.getOffsetY());
	}

	@Test
	void allowsZeroOffsetAtBottomRightOutsideVanillaHud() {
		ModuleLayout layout = new ModuleLayout(HudAnchor.BOTTOM_RIGHT, 12, -8);
		HudBounds protectedRegion = HudPlacementConstraints.vanillaHudRegion(427, 240);

		HudBounds bounds = engine.applyConstrainedDragOffset(layout, 20, 30,
			407, 210, 427, 240, protectedRegion, 1);

		assertEquals(new HudBounds(407, 210, 20, 30), bounds);
		assertEquals(0, layout.getOffsetX());
		assertEquals(0, layout.getOffsetY());
	}

	@Test
	void nudgesByExactPixelDeltaAndUpdatesAnchorRelativeOffset() {
		ModuleLayout layout = new ModuleLayout(HudAnchor.CENTER, 0, 0);
		HudBounds current = HudLayoutEngine.resolve(layout, new HudSize(20, 10), 200, 100);
		HudBounds protectedRegion = new HudBounds(0, 90, 200, 10);

		HudBounds onePixel = engine.applyNudge(layout, current, 1, -1,
			200, 100, protectedRegion);
		HudBounds fivePixels = engine.applyNudge(layout, onePixel, 5, 0,
			200, 100, protectedRegion);

		assertEquals(new HudBounds(96, 44, 20, 10), fivePixels);
		assertEquals(6, layout.getOffsetX());
		assertEquals(-1, layout.getOffsetY());
	}

	@Test
	void nudgeClampsAtScreenBounds() {
		ModuleLayout layout = new ModuleLayout(HudAnchor.TOP_LEFT, 0, 0);
		HudBounds current = new HudBounds(0, 0, 20, 10);
		HudBounds protectedRegion = new HudBounds(40, 40, 20, 10);

		HudBounds resolved = engine.applyNudge(layout, current, -5, -5,
			100, 60, protectedRegion);

		assertEquals(current, resolved);
		assertEquals(0, layout.getOffsetX());
		assertEquals(0, layout.getOffsetY());
	}

	@Test
	void nudgeDoesNotEnterProtectedRegion() {
		ModuleLayout layout = new ModuleLayout(HudAnchor.TOP_LEFT, 20, 20);
		HudBounds current = new HudBounds(20, 20, 10, 10);
		HudBounds protectedRegion = new HudBounds(30, 20, 20, 20);

		HudBounds resolved = engine.applyNudge(layout, current, 1, 0,
			100, 60, protectedRegion);

		assertEquals(current, resolved);
		assertEquals(20, layout.getOffsetX());
		assertEquals(20, layout.getOffsetY());
	}

}
