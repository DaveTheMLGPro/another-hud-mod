package net.davethemlgpro.client.module.miningsession;

final class MiningSessionMarquee {
	static final long PAUSE_NANOS = 750_000_000L;
	static final long NANOS_PER_PIXEL = 35_000_000L;

	private MiningSessionMarquee() {
	}

	static int offset(int textWidth, int availableWidth, long timeNanos) {
		int overflow = Math.max(0, textWidth - availableWidth);
		if (overflow == 0) {
			return 0;
		}

		long travelNanos = overflow * NANOS_PER_PIXEL;
		long halfCycle = PAUSE_NANOS + travelNanos;
		long phase = Math.floorMod(timeNanos, halfCycle * 2L);
		if (phase < PAUSE_NANOS) {
			return 0;
		}
		if (phase < halfCycle) {
			return -interpolate(overflow, phase - PAUSE_NANOS, travelNanos);
		}
		if (phase < halfCycle + PAUSE_NANOS) {
			return -overflow;
		}
		return -overflow + interpolate(overflow,
			phase - halfCycle - PAUSE_NANOS, travelNanos);
	}

	private static int interpolate(int distance, long elapsedNanos, long durationNanos) {
		return (int) Math.round(distance * (double) elapsedNanos / durationNanos);
	}
}
