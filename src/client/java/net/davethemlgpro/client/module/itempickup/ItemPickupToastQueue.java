package net.davethemlgpro.client.module.itempickup;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

final class ItemPickupToastQueue<T> {
	private final int capacity;
	private final Deque<Entry<T>> entries = new ArrayDeque<>();
	private final Map<Entry<T>, Long> overflowFadeStarts = new IdentityHashMap<>();
	private long nextAllowedRemovalNanos = Long.MIN_VALUE;

	ItemPickupToastQueue(int capacity) {
		if (capacity <= 0) {
			throw new IllegalArgumentException("Toast queue capacity must be positive.");
		}
		this.capacity = capacity;
	}

	void record(T value, int amount, long nowNanos, long mergeWindowNanos, BiPredicate<T, T> matcher) {
		if (amount <= 0 || mergeWindowNanos < 0L) {
			throw new IllegalArgumentException("Invalid pickup amount or merge window.");
		}

		Entry<T> matching = findMergeCandidate(value, nowNanos, mergeWindowNanos, matcher);
		boolean merged = matching != null;
		if (matching != null) {
			entries.remove(matching);
			overflowFadeStarts.remove(matching);
			amount = saturatingAdd(matching.amount(), amount);
		}
		entries.addLast(new Entry<>(value, amount, nowNanos, merged));
		while (entries.size() > capacity) {
			removeFirst();
		}
	}

	List<Entry<T>> snapshot(int maximum, long nowNanos, long displayNanos, long removeDelayNanos,
							long fadeDurationNanos, boolean fadeOut) {
		if (maximum <= 0 || displayNanos < 0L || removeDelayNanos < 0L || fadeDurationNanos < 0L) {
			throw new IllegalArgumentException("Invalid toast timing or visible limit.");
		}

		if (fadeOut) {
			markOverflowForImmediateFade(maximum, nowNanos);
			removeFinishedOverflowFades(nowNanos, fadeDurationNanos, removeDelayNanos);
		} else {
			overflowFadeStarts.clear();
			while (entries.size() > maximum) {
				removeFirst();
			}
		}
		removeExpired(nowNanos, displayNanos, removeDelayNanos, fadeDurationNanos, fadeOut);
		return List.copyOf(entries);
	}

	float opacity(Entry<T> entry, long nowNanos, long displayNanos, long fadeDurationNanos,
				  boolean fadeOut) {
		if (!fadeOut) {
			return 1.0F;
		}
		Long overflowStart = overflowFadeStarts.get(entry);
		if (overflowStart != null) {
			return fadeOpacity(overflowStart, nowNanos, fadeDurationNanos);
		}
		if (entries.isEmpty() || !overflowFadeStarts.isEmpty() || entries.getFirst() != entry) {
			return 1.0F;
		}
		long fadeStart = Math.max(saturatingAdd(entry.updatedAtNanos(), displayNanos),
			nextAllowedRemovalNanos);
		return fadeOpacity(fadeStart, nowNanos, fadeDurationNanos);
	}

	void clear() {
		entries.clear();
		overflowFadeStarts.clear();
		nextAllowedRemovalNanos = Long.MIN_VALUE;
	}

	private void markOverflowForImmediateFade(int maximum, long nowNanos) {
		int overflowCount = Math.max(0, entries.size() - maximum);
		int index = 0;
		for (Entry<T> entry : entries) {
			if (index++ >= overflowCount) {
				break;
			}
			overflowFadeStarts.putIfAbsent(entry, nowNanos);
		}
	}

	private void removeFinishedOverflowFades(long nowNanos, long fadeDurationNanos, long removeDelayNanos) {
		while (!entries.isEmpty()) {
			Long fadeStart = overflowFadeStarts.get(entries.getFirst());
			if (fadeStart == null) {
				return;
			}
			long removalAt = saturatingAdd(fadeStart, fadeDurationNanos);
			if (nowNanos < removalAt) {
				return;
			}
			removeFirst();
			nextAllowedRemovalNanos = Math.max(nextAllowedRemovalNanos,
				saturatingAdd(removalAt, removeDelayNanos));
		}
	}

	private Entry<T> findMergeCandidate(T value, long nowNanos, long mergeWindowNanos,
										BiPredicate<T, T> matcher) {
		if (mergeWindowNanos == 0L) {
			return null;
		}
		Iterator<Entry<T>> iterator = entries.descendingIterator();
		while (iterator.hasNext()) {
			Entry<T> entry = iterator.next();
			long age = elapsedNanos(entry.updatedAtNanos(), nowNanos);
			if (age > mergeWindowNanos) {
				break;
			}
			if (matcher.test(entry.value(), value)) {
				return entry;
			}
		}
		return null;
	}

	private void removeExpired(long nowNanos, long displayNanos, long removeDelayNanos,
							   long fadeDurationNanos, boolean fadeOut) {
		if (!overflowFadeStarts.isEmpty()) {
			return;
		}
		while (!entries.isEmpty()) {
			long readyAt = saturatingAdd(entries.getFirst().updatedAtNanos(), displayNanos);
			long removalStart = Math.max(readyAt, nextAllowedRemovalNanos);
			long removalAt = fadeOut ? saturatingAdd(removalStart, fadeDurationNanos) : removalStart;
			if (nowNanos < removalAt) {
				return;
			}
			removeFirst();
			nextAllowedRemovalNanos = saturatingAdd(removalAt, removeDelayNanos);
		}
		nextAllowedRemovalNanos = Long.MIN_VALUE;
	}

	private void removeFirst() {
		Entry<T> removed = entries.removeFirst();
		overflowFadeStarts.remove(removed);
	}

	private static float fadeOpacity(long fadeStart, long nowNanos, long fadeDurationNanos) {
		if (nowNanos <= fadeStart) {
			return 1.0F;
		}
		if (fadeDurationNanos == 0L) {
			return 0.0F;
		}
		return (float) Math.clamp(1.0D - (double) (nowNanos - fadeStart) / fadeDurationNanos,
			0.0D, 1.0D);
	}

	private static long elapsedNanos(long earlier, long later) {
		return later >= earlier ? later - earlier : Long.MAX_VALUE;
	}

	private static int saturatingAdd(int left, int right) {
		return left > Integer.MAX_VALUE - right ? Integer.MAX_VALUE : left + right;
	}

	private static long saturatingAdd(long left, long right) {
		if (right > 0L && left > Long.MAX_VALUE - right) {
			return Long.MAX_VALUE;
		}
		return left + right;
	}

	record Entry<T>(T value, int amount, long updatedAtNanos, boolean merged) {
	}
}
