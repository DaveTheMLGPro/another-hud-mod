package net.davethemlgpro.client.module.containersearch;

import com.google.gson.annotations.SerializedName;
import net.davethemlgpro.client.hud.layout.HudAnchor;
import net.davethemlgpro.client.hud.layout.ModuleLayout;
import net.davethemlgpro.client.module.HudModuleConfig;

public final class ContainerSearchHudConfig implements HudModuleConfig<ContainerSearchHudConfig> {
	public static final double MIN_UI_SCALE = 0.5D;
	public static final double MAX_UI_SCALE = 2.0D;
	public static final int DEFAULT_BACKGROUND_COLOR = 0xF0181818;
	public static final int DEFAULT_TEXT_COLOR = 0xFFE8E8E8;
	public static final int DEFAULT_HIGHLIGHT_COLOR = 0xFF00FFDC;
	public static final int DEFAULT_DIM_COLOR = 0x96000000;

	@SerializedName("enabled")
	private boolean visible = true;
	private ModuleLayout layout = defaultLayout();
	private double uiScale = 1.0D;
	private boolean dimNonMatches = true;
	private boolean exactMatch;
	private boolean clearOnClose;
	private ContainerSearchHighlightStyle highlightStyle = ContainerSearchHighlightStyle.OUTLINE;
	private int backgroundColor = DEFAULT_BACKGROUND_COLOR;
	private int textColor = DEFAULT_TEXT_COLOR;
	private int highlightColor = DEFAULT_HIGHLIGHT_COLOR;
	private int dimColor = DEFAULT_DIM_COLOR;

	@Override
	public boolean visible() {
		return visible;
	}

	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	@Override
	public ModuleLayout getLayout() {
		return layout;
	}

	@Override
	public ContainerSearchHudConfig copy() {
		ContainerSearchHudConfig copy = new ContainerSearchHudConfig();
		copy.copyFrom(this);
		return copy;
	}

	@Override
	public void copyFrom(ContainerSearchHudConfig source) {
		visible = source.visible;
		layout = new ModuleLayout();
		layout.copyFrom(source.layout);
		uiScale = source.uiScale;
		dimNonMatches = source.dimNonMatches;
		exactMatch = source.exactMatch;
		clearOnClose = source.clearOnClose;
		highlightStyle = source.highlightStyle;
		backgroundColor = source.backgroundColor;
		textColor = source.textColor;
		highlightColor = source.highlightColor;
		dimColor = source.dimColor;
		validate();
	}

	@Override
	public void validate() {
		if (layout == null) {
			layout = defaultLayout();
		}
		layout.validate();
		uiScale = Math.clamp(Double.isFinite(uiScale) ? uiScale : 1.0D, MIN_UI_SCALE, MAX_UI_SCALE);
		if (highlightStyle == null) {
			highlightStyle = ContainerSearchHighlightStyle.OUTLINE;
		}
	}

	public double getUiScale() {
		return uiScale;
	}

	public void setUiScale(double uiScale) {
		this.uiScale = Math.clamp(Double.isFinite(uiScale) ? uiScale : 1.0D, MIN_UI_SCALE, MAX_UI_SCALE);
	}

	public boolean isDimNonMatches() {
		return dimNonMatches;
	}

	public void setDimNonMatches(boolean dimNonMatches) {
		this.dimNonMatches = dimNonMatches;
	}

	public boolean isExactMatch() {
		return exactMatch;
	}

	public void setExactMatch(boolean exactMatch) {
		this.exactMatch = exactMatch;
	}

	public boolean isClearOnClose() {
		return clearOnClose;
	}

	public void setClearOnClose(boolean clearOnClose) {
		this.clearOnClose = clearOnClose;
	}

	public ContainerSearchHighlightStyle getHighlightStyle() {
		return highlightStyle;
	}

	public void setHighlightStyle(ContainerSearchHighlightStyle highlightStyle) {
		this.highlightStyle = highlightStyle == null
			? ContainerSearchHighlightStyle.OUTLINE : highlightStyle;
	}

	public int getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public int getTextColor() {
		return textColor;
	}

	public void setTextColor(int textColor) {
		this.textColor = textColor;
	}

	public int getHighlightColor() {
		return highlightColor;
	}

	public void setHighlightColor(int highlightColor) {
		this.highlightColor = highlightColor;
	}

	public int getDimColor() {
		return dimColor;
	}

	public void setDimColor(int dimColor) {
		this.dimColor = dimColor;
	}

	private static ModuleLayout defaultLayout() {
		return new ModuleLayout(HudAnchor.CENTER, 178, -32);
	}
}
