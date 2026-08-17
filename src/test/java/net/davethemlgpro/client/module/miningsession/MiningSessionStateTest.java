package net.davethemlgpro.client.module.miningsession;

import net.davethemlgpro.client.hud.HudSize;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MiningSessionStateTest {
	@Test
	void accumulatesOnlyWhileRunningAndPreservesTotalsAcrossPause() {
		MiningSessionState state = new MiningSessionState();

		state.startOrResume(1_000L);
		state.recordBlock("minecraft:stone");
		state.recordBlock("minecraft:stone");
		state.pause(4_000L);
		state.recordBlock("minecraft:diamond_ore");

		assertEquals(MiningSessionStatus.PAUSED, state.status());
		assertEquals(3_000L, state.elapsedNanos(9_000L));
		assertEquals(java.util.Map.of("minecraft:stone", 2), state.minedBlocks());

		state.startOrResume(10_000L);
		state.recordBlock("minecraft:diamond_ore");
		assertEquals(5_000L, state.elapsedNanos(12_000L));
		assertEquals(1, state.minedBlocks().get("minecraft:diamond_ore"));
	}

	@Test
	void resetClearsTimerBlocksAndStatus() {
		MiningSessionState state = new MiningSessionState();
		state.startOrResume(100L);
		state.recordBlock("minecraft:deepslate");
		state.reset();

		assertEquals(MiningSessionStatus.IDLE, state.status());
		assertEquals(0L, state.elapsedNanos(10_000L));
		assertEquals(java.util.Map.of(), state.minedBlocks());
	}

	@Test
	void formatsLongSessionsWithoutWrappingHours() {
		assertEquals("01:24:37", MiningSessionHudModule.formatTime(5_077L));
		assertEquals("125:01:02", MiningSessionHudModule.formatTime(450_062L));
	}

	@Test
	void calculatesAndFormatsExactInventoryValues() {
		BigDecimal total = MiningSessionHudModule.calculateValue(new BigDecimal("10"), 111);
		assertEquals(new BigDecimal("1110"), total);
		assertEquals("1,110", MiningSessionHudModule.formatValue(total));
		assertEquals("1,000,000.24",
			MiningSessionHudModule.formatValue(new BigDecimal("1000000.24")));
	}

	@Test
	void inventoryProgressCountsPositiveGainsWithoutRegressingAfterDeposits() {
		java.util.Map<String, Integer> gains = new java.util.HashMap<>();

		assertTrue(MiningSessionHudModule.addPositiveDeltas(
			java.util.Map.of("minecraft:diamond", 4),
			java.util.Map.of("minecraft:diamond", 11), gains));
		assertEquals(7, gains.get("minecraft:diamond"));
		assertFalse(MiningSessionHudModule.addPositiveDeltas(
			java.util.Map.of("minecraft:diamond", 11),
			java.util.Map.of("minecraft:diamond", 2), gains));
		assertEquals(7, gains.get("minecraft:diamond"));
	}

	@Test
	void goalWindowAnimatesToTheEndThenBackToTheStart() {
		long rowTime = 2_000_000_000L;
		assertEquals(0.0D, MiningSessionHudModule.animatedGoalOffset(8, 3, 0L));
		assertEquals(2.5D, MiningSessionHudModule.animatedGoalOffset(8, 3, rowTime * 5 / 2));
		assertEquals(5.0D, MiningSessionHudModule.animatedGoalOffset(8, 3, rowTime * 5));
		assertEquals(2.5D, MiningSessionHudModule.animatedGoalOffset(8, 3, rowTime * 15 / 2));
		assertEquals(0.0D, MiningSessionHudModule.animatedGoalOffset(3, 3, rowTime));
	}

	@Test
	void oversizedPanelsScaleDownToRemainInsideTheGui() {
		assertEquals(1.5D, MiningSessionHudModule.fitScale(1.5D,
			new HudSize(100, 100), 320, 240));
		assertEquals(0.8D, MiningSessionHudModule.fitScale(2.0D,
			new HudSize(200, 300), 320, 240));
		assertEquals(0.5D, MiningSessionHudModule.fitScale(1.0D,
			new HudSize(640, 200), 320, 240));
	}
}
