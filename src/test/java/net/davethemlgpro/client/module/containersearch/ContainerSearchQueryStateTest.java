package net.davethemlgpro.client.module.containersearch;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContainerSearchQueryStateTest {
	@Test
	void retainsQueryAcrossConsumersUntilConnectionIsCleared() {
		ContainerSearchQueryState state = new ContainerSearchQueryState();
		state.set("gray stained glass");

		assertEquals("gray stained glass", state.get());

		state.clear();
		assertEquals("", state.get());
	}

	@Test
	void nullQueryIsNormalizedToEmpty() {
		ContainerSearchQueryState state = new ContainerSearchQueryState();
		state.set(null);

		assertEquals("", state.get());
	}
}
