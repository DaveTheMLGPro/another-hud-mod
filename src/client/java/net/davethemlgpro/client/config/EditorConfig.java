package net.davethemlgpro.client.config;

public class EditorConfig {
	public static final int DEFAULT_SELECTION_COLOR = 0xFF66AAFF;
	public static final int DEFAULT_HOVERED_SELECTION_COLOR = 0xFFFFFFFF;
	public static final int DEFAULT_HIDDEN_OVERLAY_COLOR = 0x22444444;
	public static final int DEFAULT_MINOR_GRID_COLOR = 0x33FFFFFF;
	public static final int DEFAULT_MAJOR_GRID_COLOR = 0x55FFFFFF;
	public static final int DEFAULT_CENTER_GUIDE_COLOR = 0x88FFFFFF;

	private int selectionColor = DEFAULT_SELECTION_COLOR;
	private int hoveredSelectionColor = DEFAULT_HOVERED_SELECTION_COLOR;
	private int hiddenOverlayColor = DEFAULT_HIDDEN_OVERLAY_COLOR;
	private int minorGridColor = DEFAULT_MINOR_GRID_COLOR;
	private int majorGridColor = DEFAULT_MAJOR_GRID_COLOR;
	private int centerGuideColor = DEFAULT_CENTER_GUIDE_COLOR;

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

	public void setMinorGridColor(int minorGridColor) {
		this.minorGridColor = minorGridColor;
	}

	public int getMajorGridColor() {
		return majorGridColor;
	}

	public void setMajorGridColor(int majorGridColor) {
		this.majorGridColor = majorGridColor;
	}

	public int getCenterGuideColor() {
		return centerGuideColor;
	}

	public void setCenterGuideColor(int centerGuideColor) {
		this.centerGuideColor = centerGuideColor;
	}
}
