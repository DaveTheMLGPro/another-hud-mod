package net.davethemlgpro.client.module.itempickup;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemPickupToastQueueTest {
	private static final long SECOND = 1_000_000_000L;

	@Test
	void appendsNewPickupsAtBottomAndTrimsFromTop() {
		ItemPickupToastQueue<String> queue = new ItemPickupToastQueue<>(3);
		queue.record("sand", 1, 0L, SECOND, String::equals);
		queue.record("gravel", 1, SECOND * 2, SECOND, String::equals);
		queue.record("diamond", 1, SECOND * 4, SECOND, String::equals);
		queue.record("iron", 1, SECOND * 6, SECOND, String::equals);

		List<ItemPickupToastQueue.Entry<String>> entries =
			queue.snapshot(3, SECOND * 6, SECOND * 20, SECOND, SECOND, false);

		assertEquals(List.of("gravel", "diamond", "iron"),
			entries.stream().map(ItemPickupToastQueue.Entry::value).toList());
	}

	@Test
	void mergesMatchingPickupMovesItToBottomAndResetsItsTimer() {
		ItemPickupToastQueue<String> queue = new ItemPickupToastQueue<>(3);
		queue.record("sand", 1, 0L, SECOND, String::equals);
		queue.record("gravel", 2, SECOND / 2, SECOND, String::equals);
		queue.record("sand", 3, SECOND, SECOND, String::equals);

		List<ItemPickupToastQueue.Entry<String>> entries =
			queue.snapshot(3, SECOND * 3, SECOND * 3, SECOND / 2, SECOND, false);

		assertEquals(List.of("gravel", "sand"),
			entries.stream().map(ItemPickupToastQueue.Entry::value).toList());
		assertEquals(4, entries.getLast().amount());
		assertEquals(true, entries.getLast().merged());

		entries = queue.snapshot(3, SECOND * 4 - 1, SECOND * 3, SECOND / 2, SECOND, false);
		assertEquals(List.of("sand"),
			entries.stream().map(ItemPickupToastQueue.Entry::value).toList());
		assertEquals(4, entries.getFirst().amount());
	}

	@Test
	void doesNotMergeOutsideWindowOrWhenDisabled() {
		ItemPickupToastQueue<String> queue = new ItemPickupToastQueue<>(4);
		queue.record("sand", 1, 0L, SECOND, String::equals);
		queue.record("sand", 2, SECOND + 1, SECOND, String::equals);
		queue.record("sand", 4, SECOND + 1, 0L, String::equals);

		List<ItemPickupToastQueue.Entry<String>> entries =
			queue.snapshot(4, SECOND + 1, SECOND * 20, SECOND, SECOND, false);

		assertEquals(List.of(1, 2, 4),
			entries.stream().map(ItemPickupToastQueue.Entry::amount).toList());
	}

	@Test
	void removesOldestEntriesAtConfiguredCadence() {
		ItemPickupToastQueue<String> queue = new ItemPickupToastQueue<>(3);
		queue.record("sand", 1, 0L, 0L, String::equals);
		queue.record("gravel", 1, 0L, 0L, String::equals);
		queue.record("iron", 1, 0L, 0L, String::equals);

		assertEquals(List.of("gravel", "iron"),
			values(queue.snapshot(3, SECOND * 3, SECOND * 3, SECOND / 2, SECOND, false)));
		assertEquals(List.of("gravel", "iron"),
			values(queue.snapshot(3, SECOND * 3 + SECOND / 2 - 1,
				SECOND * 3, SECOND / 2, SECOND, false)));
		assertEquals(List.of("iron"),
			values(queue.snapshot(3, SECOND * 3 + SECOND / 2,
				SECOND * 3, SECOND / 2, SECOND, false)));
	}

	@Test
	void fadesOldestEntryDuringRemovalDelay() {
		ItemPickupToastQueue<String> queue = new ItemPickupToastQueue<>(3);
		queue.record("sand", 1, 0L, 0L, String::equals);
		queue.record("iron", 1, 0L, 0L, String::equals);

		List<ItemPickupToastQueue.Entry<String>> entries =
			queue.snapshot(3, SECOND * 3, SECOND * 3, SECOND / 2, SECOND, true);
		assertEquals(1.0F, queue.opacity(entries.getFirst(), SECOND * 3, SECOND * 3, SECOND, true));
		assertEquals(0.5F, queue.opacity(entries.getFirst(), SECOND * 3 + SECOND / 2,
			SECOND * 3, SECOND, true), 0.001F);
		assertEquals(1.0F, queue.opacity(entries.getLast(), SECOND * 3 + SECOND / 2,
			SECOND * 3, SECOND, true));

		entries = queue.snapshot(3, SECOND * 4, SECOND * 3, SECOND / 2, SECOND, true);
		assertEquals(List.of("iron"), values(entries));
		assertEquals(1.0F, queue.opacity(entries.getFirst(), SECOND * 4, SECOND * 3, SECOND, true));
	}

	@Test
	void overflowEntryFadesImmediatelyAboveVisibleCapacity() {
		ItemPickupToastQueue<String> queue = new ItemPickupToastQueue<>(10);
		queue.record("sand", 1, 0L, 0L, String::equals);
		queue.record("gravel", 1, 0L, 0L, String::equals);
		queue.record("iron", 1, 0L, 0L, String::equals);
		queue.snapshot(3, 0L, SECOND * 10, SECOND / 2, SECOND, true);

		queue.record("diamond", 1, SECOND, 0L, String::equals);
		List<ItemPickupToastQueue.Entry<String>> entries =
			queue.snapshot(3, SECOND, SECOND * 10, SECOND / 2, SECOND, true);

		assertEquals(List.of("sand", "gravel", "iron", "diamond"), values(entries));
		assertEquals(1.0F, queue.opacity(entries.getFirst(), SECOND, SECOND * 10, SECOND, true));
		assertEquals(0.5F, queue.opacity(entries.getFirst(), SECOND + SECOND / 2,
			SECOND * 10, SECOND, true), 0.001F);

		entries = queue.snapshot(3, SECOND * 2, SECOND * 10, SECOND / 2, SECOND, true);
		assertEquals(List.of("gravel", "iron", "diamond"), values(entries));
	}

	private static List<String> values(List<ItemPickupToastQueue.Entry<String>> entries) {
		return entries.stream().map(ItemPickupToastQueue.Entry::value).toList();
	}
}
