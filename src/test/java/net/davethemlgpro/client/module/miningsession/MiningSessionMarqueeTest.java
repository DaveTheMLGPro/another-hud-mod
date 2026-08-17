package net.davethemlgpro.client.module.miningsession;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MiningSessionMarqueeTest {
	@Test
	void keepsTextStillWhenItFits() {
		assertEquals(0, MiningSessionMarquee.offset(80, 80, Long.MAX_VALUE));
		assertEquals(0, MiningSessionMarquee.offset(60, 80, Long.MAX_VALUE));
	}

	@Test
	void pausesAtBothEndsAndMovesInBothDirections() {
		int overflow = 20;
		long travel = overflow * MiningSessionMarquee.NANOS_PER_PIXEL;
		long halfCycle = MiningSessionMarquee.PAUSE_NANOS + travel;

		assertEquals(0, MiningSessionMarquee.offset(100, 80, 0));
		assertEquals(0, MiningSessionMarquee.offset(100, 80, MiningSessionMarquee.PAUSE_NANOS - 1));
		assertEquals(-10, MiningSessionMarquee.offset(100, 80,
			MiningSessionMarquee.PAUSE_NANOS + travel / 2));
		assertEquals(-20, MiningSessionMarquee.offset(100, 80, halfCycle));
		assertEquals(-20, MiningSessionMarquee.offset(100, 80,
			halfCycle + MiningSessionMarquee.PAUSE_NANOS - 1));
		assertEquals(-10, MiningSessionMarquee.offset(100, 80,
			halfCycle + MiningSessionMarquee.PAUSE_NANOS + travel / 2));
	}
}
