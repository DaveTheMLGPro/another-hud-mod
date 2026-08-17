package net.davethemlgpro.client.module.miningsession;

import java.util.LinkedHashMap;
import java.util.Map;

public final class MiningSessionState {
	private final Map<String, Integer> minedBlocks = new LinkedHashMap<>();
	private MiningSessionStatus status = MiningSessionStatus.IDLE;
	private long accumulatedNanos;
	private long resumedAtNanos;

	public MiningSessionStatus status() {
		return status;
	}

	public void toggle(long nowNanos) {
		if (status == MiningSessionStatus.RUNNING) {
			pause(nowNanos);
		} else {
			startOrResume(nowNanos);
		}
	}

	public void startOrResume(long nowNanos) {
		if (status == MiningSessionStatus.RUNNING) {
			return;
		}
		resumedAtNanos = nowNanos;
		status = MiningSessionStatus.RUNNING;
	}

	public void pause(long nowNanos) {
		if (status != MiningSessionStatus.RUNNING) {
			return;
		}
		accumulatedNanos += Math.max(0L, nowNanos - resumedAtNanos);
		status = MiningSessionStatus.PAUSED;
	}

	public void reset() {
		minedBlocks.clear();
		status = MiningSessionStatus.IDLE;
		accumulatedNanos = 0L;
		resumedAtNanos = 0L;
	}

	public void recordBlock(String blockId) {
		if (status != MiningSessionStatus.RUNNING || blockId == null || blockId.isBlank()) {
			return;
		}
		minedBlocks.merge(blockId, 1, Integer::sum);
	}

	public long elapsedNanos(long nowNanos) {
		return accumulatedNanos + (status == MiningSessionStatus.RUNNING
			? Math.max(0L, nowNanos - resumedAtNanos) : 0L);
	}

	public Map<String, Integer> minedBlocks() {
		return Map.copyOf(minedBlocks);
	}
}
