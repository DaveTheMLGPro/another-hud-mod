package net.davethemlgpro.client.module.armor;

import net.davethemlgpro.client.hud.layout.HudAnchor;
import net.davethemlgpro.client.hud.layout.ModuleLayout;
import net.davethemlgpro.client.module.HudModuleConfig;

public final class ArmorHudConfig implements HudModuleConfig<ArmorHudConfig> {
	public static final float MIN_SCALE = 0.75F;
	public static final float MAX_SCALE = 3.0F;
	public static final int MIN_SPACING = 0;
	public static final int MAX_SPACING = 8;
	public static final int MIN_DURABILITY_BAR_HEIGHT = 1;
	public static final int MAX_DURABILITY_BAR_HEIGHT = 6;
	public static final float MAX_DURABILITY_BAR_HORIZONTAL_PADDING = 8.0F;

	private boolean enabled = true;
	private ModuleLayout layout = new ModuleLayout(HudAnchor.CENTER_RIGHT, -8, 0);
	private ArmorHudOrientation orientation = ArmorHudOrientation.VERTICAL;
	private int spacing = 2;
	private float scale = 1.0F;
	private boolean showEmptySlots;
	private int emptySlotBackgroundColor = 0x33000000;

	private boolean durabilityBarVisible = true;
	private int durabilityBarHeight = 2;
	private float durabilityBarHorizontalPadding = 2.0F;
	private int durabilityBackgroundColor = 0xFF000000;
	private int durabilityHealthyColor = 0xFF00FF00;
	private int durabilityWarningColor = 0xFFFFFF00;
	private int durabilityCriticalColor = 0xFFFF0000;

	public ArmorHudConfig() {
	}

	@Override
	public boolean enabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public ModuleLayout getLayout() {
		return layout;
	}

	@Override
	public ArmorHudConfig copy() {
		ArmorHudConfig copy = new ArmorHudConfig();
		copy.copyFrom(this);
		return copy;
	}

	@Override
	public void copyFrom(ArmorHudConfig source) {
		enabled = source.enabled;
		layout = new ModuleLayout();
		layout.copyFrom(source.layout);
		orientation = source.orientation;
		spacing = source.spacing;
		scale = source.scale;
		showEmptySlots = source.showEmptySlots;
		emptySlotBackgroundColor = source.emptySlotBackgroundColor;
		durabilityBarVisible = source.durabilityBarVisible;
		durabilityBarHeight = source.durabilityBarHeight;
		durabilityBarHorizontalPadding = source.durabilityBarHorizontalPadding;
		durabilityBackgroundColor = source.durabilityBackgroundColor;
		durabilityHealthyColor = source.durabilityHealthyColor;
		durabilityWarningColor = source.durabilityWarningColor;
		durabilityCriticalColor = source.durabilityCriticalColor;
		validate();
	}

	@Override
	public void validate() {
		if (layout == null) {
			layout = new ModuleLayout(HudAnchor.CENTER_RIGHT, -8, 0);
		}
		layout.validate();
		if (orientation == null) {
			orientation = ArmorHudOrientation.VERTICAL;
		}
		spacing = Math.clamp(spacing, MIN_SPACING, MAX_SPACING);
		scale = Float.isFinite(scale) ? Math.clamp(scale, MIN_SCALE, MAX_SCALE) : 1.0F;
		durabilityBarHeight = Math.clamp(durabilityBarHeight, MIN_DURABILITY_BAR_HEIGHT, MAX_DURABILITY_BAR_HEIGHT);
		durabilityBarHorizontalPadding = Float.isFinite(durabilityBarHorizontalPadding)
			? Math.clamp(durabilityBarHorizontalPadding, 0.0F, MAX_DURABILITY_BAR_HORIZONTAL_PADDING) : 2.0F;
	}

	public ArmorHudOrientation getOrientation() {
		return orientation;
	}

	public void setOrientation(ArmorHudOrientation orientation) {
		this.orientation = orientation == null ? ArmorHudOrientation.VERTICAL : orientation;
	}

	public int getSpacing() {
		return spacing;
	}

	public void setSpacing(int spacing) {
		this.spacing = Math.clamp(spacing, MIN_SPACING, MAX_SPACING);
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = Float.isFinite(scale) ? Math.clamp(scale, MIN_SCALE, MAX_SCALE) : 1.0F;
	}

	public boolean isShowEmptySlots() {
		return showEmptySlots;
	}

	public void setShowEmptySlots(boolean showEmptySlots) {
		this.showEmptySlots = showEmptySlots;
	}

	public int getEmptySlotBackgroundColor() {
		return emptySlotBackgroundColor;
	}

	public void setEmptySlotBackgroundColor(int emptySlotBackgroundColor) {
		this.emptySlotBackgroundColor = emptySlotBackgroundColor;
	}

	public boolean isDurabilityBarVisible() {
		return durabilityBarVisible;
	}

	public void setDurabilityBarVisible(boolean durabilityBarVisible) {
		this.durabilityBarVisible = durabilityBarVisible;
	}

	public int getDurabilityBarHeight() {
		return durabilityBarHeight;
	}

	public void setDurabilityBarHeight(int durabilityBarHeight) {
		this.durabilityBarHeight = Math.clamp(durabilityBarHeight, MIN_DURABILITY_BAR_HEIGHT, MAX_DURABILITY_BAR_HEIGHT);
	}

	public float getDurabilityBarHorizontalPadding() {
		return durabilityBarHorizontalPadding;
	}

	public void setDurabilityBarHorizontalPadding(float durabilityBarHorizontalPadding) {
		this.durabilityBarHorizontalPadding = Float.isFinite(durabilityBarHorizontalPadding)
				? Math.clamp(durabilityBarHorizontalPadding, 0.0F, MAX_DURABILITY_BAR_HORIZONTAL_PADDING) : 2.0F;
	}

	public int getDurabilityBackgroundColor() {
		return durabilityBackgroundColor;
	}

	public void setDurabilityBackgroundColor(int durabilityBackgroundColor) {
		this.durabilityBackgroundColor = durabilityBackgroundColor;
	}

	public int getDurabilityHealthyColor() {
		return durabilityHealthyColor;
	}

	public void setDurabilityHealthyColor(int durabilityHealthyColor) {
		this.durabilityHealthyColor = durabilityHealthyColor;
	}

	public int getDurabilityWarningColor() {
		return durabilityWarningColor;
	}

	public void setDurabilityWarningColor(int durabilityWarningColor) {
		this.durabilityWarningColor = durabilityWarningColor;
	}

	public int getDurabilityCriticalColor() {
		return durabilityCriticalColor;
	}

	public void setDurabilityCriticalColor(int durabilityCriticalColor) {
		this.durabilityCriticalColor = durabilityCriticalColor;
	}
}
