package net.davethemlgpro.client.config;

public class EditorConfig {
	private int selectionColor = 0xFF66AAFF;
	private int hoveredSelectionColor = 0xFFFFFFFF;
	private int hiddenOverlayColor = 0x22444444;
	private int minorGridColor = 0x33FFFFFF;
	private int majorGridColor = 0x55FFFFFF;
	private int centerGuideColor = 0x88FFFFFF;

	public EditorConfig copy() {
		EditorConfig copy = new EditorConfig();
		copy.copyFrom(this);
		return copy;
	}

	public void copyFrom(EditorConfig source) {
		this.selectionColor = source.selectionColor;
		this.hoveredSelectionColor = source.hoveredSelectionColor;
		this.hiddenOverlayColor = source.hiddenOverlayColor;
		this.minorGridColor = source.minorGridColor;
		this.majorGridColor = source.majorGridColor;
		this.centerGuideColor = source.centerGuideColor;
	}

	public void validate() {
		// Every int bit-pattern is a valid ARGB color.

		// Add other checks if needed
	}

	public int getSelectionColor() {
		return selectionColor;
	}

	public int getHoveredSelectionColor() {
		return hoveredSelectionColor;
	}

	public int getHiddenOverlayColor() {
		return hiddenOverlayColor;
	}

	public int getMinorGridColor() {
		return minorGridColor;
	}

	public int getMajorGridColor() {
		return majorGridColor;
	}

	public int getCenterGuideColor() {
		return centerGuideColor;
	}
}
