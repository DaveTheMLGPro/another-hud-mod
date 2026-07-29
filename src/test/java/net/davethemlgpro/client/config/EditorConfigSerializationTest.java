package net.davethemlgpro.client.config;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EditorConfigSerializationTest {
	private final Gson gson = new Gson();

	@Test
	void roundTripsGridColors() {
		EditorConfig original = new EditorConfig();
		original.setMinorGridColor(0x11223344);
		original.setMajorGridColor(0x55667788);
		original.setCenterGuideColor(0x99AABBCC);

		EditorConfig restored = gson.fromJson(gson.toJson(original), EditorConfig.class);

		assertEquals(0x11223344, restored.getMinorGridColor());
		assertEquals(0x55667788, restored.getMajorGridColor());
		assertEquals(0x99AABBCC, restored.getCenterGuideColor());
	}

	@Test
	void missingGridColorsUseDefaults() {
		EditorConfig restored = gson.fromJson("{}", EditorConfig.class);

		assertEquals(EditorConfig.DEFAULT_MINOR_GRID_COLOR, restored.getMinorGridColor());
		assertEquals(EditorConfig.DEFAULT_MAJOR_GRID_COLOR, restored.getMajorGridColor());
		assertEquals(EditorConfig.DEFAULT_CENTER_GUIDE_COLOR, restored.getCenterGuideColor());
	}
}
