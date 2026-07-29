package net.davethemlgpro.client.hud.layout;

import net.davethemlgpro.client.hud.HudBounds;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class HudPlacementConstraintsTest {
	@Test
	void centersVanillaHudRegionInScaledScreenCoordinates() {
		assertEquals(new HudBounds(122, 191, 182, 49),
			HudPlacementConstraints.vanillaHudRegion(427, 240));
		assertEquals(new HudBounds(309, 401, 182, 49),
			HudPlacementConstraints.vanillaHudRegion(800, 450));
	}

	@Test
	void fitsProtectedRegionInsideSmallGui() {
		assertEquals(new HudBounds(0, 41, 160, 49),
			HudPlacementConstraints.vanillaHudRegion(160, 90));
		assertEquals(new HudBounds(0, 0, 40, 30),
			HudPlacementConstraints.vanillaHudRegion(40, 30));
	}

	@Test
	void leavesBottomCornersAvailable() {
		HudBounds protectedRegion = HudPlacementConstraints.vanillaHudRegion(427, 240);
		HudBounds bottomLeft = new HudBounds(0, 210, 20, 30);
		HudBounds bottomRight = new HudBounds(407, 210, 20, 30);

		assertEquals(bottomLeft,
			HudPlacementConstraints.avoid(bottomLeft, protectedRegion, 427, 240, 1));
		assertEquals(bottomRight,
			HudPlacementConstraints.avoid(bottomRight, protectedRegion, 427, 240, 1));
	}

	@Test
	void movesCentralBottomPlacementAboveVanillaHud() {
		HudBounds protectedRegion = HudPlacementConstraints.vanillaHudRegion(427, 240);

		HudBounds resolved = HudPlacementConstraints.avoid(
			new HudBounds(200, 210, 20, 30), protectedRegion, 427, 240, 1);

		assertEquals(new HudBounds(200, 161, 20, 30), resolved);
		assertFalse(resolved.intersects(protectedRegion));
	}

	@Test
	void preservesGridAlignmentWhenAvoidingVanillaHud() {
		HudBounds protectedRegion = HudPlacementConstraints.vanillaHudRegion(427, 240);

		HudBounds resolved = HudPlacementConstraints.avoid(
			new HudBounds(200, 210, 20, 20), protectedRegion, 427, 240, 5);

		assertEquals(new HudBounds(200, 170, 20, 20), resolved);
		assertFalse(resolved.intersects(protectedRegion));
	}

	@Test
	void choosesNearestOpenSideOfProtectedRegion() {
		HudBounds protectedRegion = HudPlacementConstraints.vanillaHudRegion(427, 240);

		HudBounds resolved = HudPlacementConstraints.avoid(
			new HudBounds(115, 210, 20, 20), protectedRegion, 427, 240, 1);

		assertEquals(new HudBounds(102, 210, 20, 20), resolved);
		assertFalse(resolved.intersects(protectedRegion));
	}
}
