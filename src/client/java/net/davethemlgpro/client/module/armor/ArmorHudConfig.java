package net.davethemlgpro.client.module.armor;

import net.davethemlgpro.client.hud.layout.HudAnchor;
import net.davethemlgpro.client.hud.layout.ModuleLayout;
import net.davethemlgpro.client.module.HudModuleConfig;

public final class ArmorHudConfig implements HudModuleConfig<ArmorHudConfig> {
	public static final int ARMOR_SLOT_COUNT = 4;
	public static final float MIN_SCALE = 0.75F;
	public static final float MAX_SCALE = 3.0F;
	public static final int MIN_SPACING = 0;
	public static final int MAX_SPACING = 8;
	public static final int MIN_DURABILITY_BAR_HEIGHT = 1;
	public static final int MAX_DURABILITY_BAR_HEIGHT = 6;
	public static final float MAX_DURABILITY_BAR_HORIZONTAL_PADDING = 8.0F;
	public static final float MIN_DURABILITY_TEXT_SCALE = 0.25F;
	public static final float MAX_DURABILITY_TEXT_SCALE = 0.75F;
	public static final int MIN_LOW_DURABILITY_THRESHOLD_PERCENT = 1;
	public static final int MAX_LOW_DURABILITY_THRESHOLD_PERCENT = 100;
	public static final int DEFAULT_DURABILITY_BACKGROUND_COLOR = 0xFF000000;
	public static final int DEFAULT_DURABILITY_HEALTHY_COLOR = 0xFF00FF00;
	public static final int DEFAULT_DURABILITY_WARNING_COLOR = 0xFFFFFF00;
	public static final int DEFAULT_DURABILITY_CRITICAL_COLOR = 0xFFFF0000;
	public static final int DEFAULT_DURABILITY_TEXT_COLOR = 0xFFFFFFFF;
	public static final int DEFAULT_TEXT_HEALTHY_COLOR = 0xFF00FF00;
	public static final int DEFAULT_TEXT_WARNING_COLOR = 0xFFFFFF00;
	public static final int DEFAULT_TEXT_CRITICAL_COLOR = 0xFFFF0000;
	public static final int DEFAULT_LOW_DURABILITY_WARNING_COLOR = 0xFFFF3333;

	private boolean enabled = true;
	private ModuleLayout layout = new ModuleLayout(HudAnchor.CENTER_RIGHT, -8, 0);
	private ArmorHudLayoutMode layoutMode = ArmorHudLayoutMode.GROUPED;
	private ModuleLayout[] individualLayouts = createDefaultIndividualLayouts();
	private boolean[] individualSlotVisible = createDefaultIndividualSlotVisibility();
	private ArmorHudOrientation orientation = ArmorHudOrientation.VERTICAL;
	private int spacing = 2;
	private float scale = 1.0F;
	private boolean showEmptySlots;
	private boolean centerVisibleSlots;
	private ArmorHudSlotStyle slotStyle = ArmorHudSlotStyle.CLEAR;

	private boolean durabilityBarVisible = true;
	private int durabilityBarHeight = 2;
	private float durabilityBarHorizontalPadding = 2.0F;
	private int durabilityBackgroundColor = DEFAULT_DURABILITY_BACKGROUND_COLOR;
	private int durabilityHealthyColor = DEFAULT_DURABILITY_HEALTHY_COLOR;
	private int durabilityWarningColor = DEFAULT_DURABILITY_WARNING_COLOR;
	private int durabilityCriticalColor = DEFAULT_DURABILITY_CRITICAL_COLOR;

	private ArmorHudDurabilityMode durabilityMode = ArmorHudDurabilityMode.PERCENT;
	private ArmorHudTextPosition textPosition = ArmorHudTextPosition.BOTTOM;
	private float durabilityTextScale = 0.5F;
	private boolean durabilityTextShadow = true;
	private int durabilityTextColor = DEFAULT_DURABILITY_TEXT_COLOR;
	private boolean colorBasedDurabilityText = true;
	private int textHealthyColor = DEFAULT_TEXT_HEALTHY_COLOR;
	private int textWarningColor = DEFAULT_TEXT_WARNING_COLOR;
	private int textCriticalColor = DEFAULT_TEXT_CRITICAL_COLOR;

	private boolean lowDurabilityWarningEnabled = true;
	private int lowDurabilityThresholdPercent = 20;
	private ArmorHudWarningStyle warningStyle = ArmorHudWarningStyle.PULSE;
	private int lowDurabilityWarningColor = DEFAULT_LOW_DURABILITY_WARNING_COLOR;

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
		layoutMode = source.layoutMode;
		individualLayouts = copyIndividualLayouts(source.individualLayouts);
		individualSlotVisible = copyIndividualSlotVisibility(source.individualSlotVisible);
		orientation = source.orientation;
		spacing = source.spacing;
		scale = source.scale;
		showEmptySlots = source.showEmptySlots;
		centerVisibleSlots = source.centerVisibleSlots;
		slotStyle = source.slotStyle;
		durabilityBarVisible = source.durabilityBarVisible;
		durabilityBarHeight = source.durabilityBarHeight;
		durabilityBarHorizontalPadding = source.durabilityBarHorizontalPadding;
		durabilityBackgroundColor = source.durabilityBackgroundColor;
		durabilityHealthyColor = source.durabilityHealthyColor;
		durabilityWarningColor = source.durabilityWarningColor;
		durabilityCriticalColor = source.durabilityCriticalColor;
		durabilityMode = source.durabilityMode;
		textPosition = source.textPosition;
		durabilityTextScale = source.durabilityTextScale;
		durabilityTextShadow = source.durabilityTextShadow;
		durabilityTextColor = source.durabilityTextColor;
		colorBasedDurabilityText = source.colorBasedDurabilityText;
		textHealthyColor = source.textHealthyColor;
		textWarningColor = source.textWarningColor;
		textCriticalColor = source.textCriticalColor;
		lowDurabilityWarningEnabled = source.lowDurabilityWarningEnabled;
		lowDurabilityThresholdPercent = source.lowDurabilityThresholdPercent;
		warningStyle = source.warningStyle;
		lowDurabilityWarningColor = source.lowDurabilityWarningColor;
		validate();
	}

	@Override
	public void validate() {
		if (layout == null) {
			layout = new ModuleLayout(HudAnchor.CENTER_RIGHT, -8, 0);
		}
		layout.validate();
		if (layoutMode == null) {
			layoutMode = ArmorHudLayoutMode.GROUPED;
		}
		individualLayouts = validatedIndividualLayouts(individualLayouts);
		individualSlotVisible = copyIndividualSlotVisibility(individualSlotVisible);
		if (orientation == null) {
			orientation = ArmorHudOrientation.VERTICAL;
		}
		if (durabilityMode == null) {
			durabilityMode = ArmorHudDurabilityMode.PERCENT;
		}
		if (textPosition == null) {
			textPosition = ArmorHudTextPosition.BOTTOM;
		}
		if (slotStyle == null) {
			slotStyle = ArmorHudSlotStyle.CLEAR;
		}
		if (warningStyle == null) {
			warningStyle = ArmorHudWarningStyle.PULSE;
		}
		spacing = Math.clamp(spacing, MIN_SPACING, MAX_SPACING);
		scale = Float.isFinite(scale) ? Math.clamp(scale, MIN_SCALE, MAX_SCALE) : 1.0F;
		durabilityBarHeight = Math.clamp(durabilityBarHeight, MIN_DURABILITY_BAR_HEIGHT, MAX_DURABILITY_BAR_HEIGHT);
		durabilityBarHorizontalPadding = Float.isFinite(durabilityBarHorizontalPadding)
			? Math.clamp(durabilityBarHorizontalPadding, 0.0F, MAX_DURABILITY_BAR_HORIZONTAL_PADDING) : 2.0F;
		durabilityTextScale = Float.isFinite(durabilityTextScale)
			? Math.clamp(durabilityTextScale, MIN_DURABILITY_TEXT_SCALE, MAX_DURABILITY_TEXT_SCALE) : 0.5F;
		lowDurabilityThresholdPercent = Math.clamp(lowDurabilityThresholdPercent,
			MIN_LOW_DURABILITY_THRESHOLD_PERCENT, MAX_LOW_DURABILITY_THRESHOLD_PERCENT);
	}

	public ArmorHudOrientation getOrientation() {
		return orientation;
	}

	public void setOrientation(ArmorHudOrientation orientation) {
		this.orientation = orientation == null ? ArmorHudOrientation.VERTICAL : orientation;
	}

	public ArmorHudLayoutMode getLayoutMode() {
		return layoutMode;
	}

	public void setLayoutMode(ArmorHudLayoutMode layoutMode) {
		this.layoutMode = layoutMode == null ? ArmorHudLayoutMode.GROUPED : layoutMode;
	}

	public ModuleLayout getIndividualLayout(int slotIndex) {
		requireArmorSlot(slotIndex);
		return individualLayouts[slotIndex];
	}

	public boolean isIndividualSlotVisible(int slotIndex) {
		requireArmorSlot(slotIndex);
		return individualSlotVisible[slotIndex];
	}

	public void setIndividualSlotVisible(int slotIndex, boolean visible) {
		requireArmorSlot(slotIndex);
		individualSlotVisible[slotIndex] = visible;
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

	public boolean isCenterVisibleSlots() {
		return centerVisibleSlots;
	}

	public void setCenterVisibleSlots(boolean centerVisibleSlots) {
		this.centerVisibleSlots = centerVisibleSlots;
	}

	public ArmorHudSlotStyle getSlotStyle() {
		return slotStyle;
	}

	public void setSlotStyle(ArmorHudSlotStyle slotStyle) {
		this.slotStyle = slotStyle == null ? ArmorHudSlotStyle.CLEAR : slotStyle;
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

	public ArmorHudDurabilityMode getDurabilityMode() {
		return durabilityMode;
	}

	public void setDurabilityMode(ArmorHudDurabilityMode durabilityMode) {
		this.durabilityMode = durabilityMode == null ? ArmorHudDurabilityMode.PERCENT : durabilityMode;
	}

	public ArmorHudTextPosition getTextPosition() {
		return textPosition;
	}

	public void setTextPosition(ArmorHudTextPosition textPosition) {
		this.textPosition = textPosition == null ? ArmorHudTextPosition.BOTTOM : textPosition;
	}

	public float getDurabilityTextScale() {
		return durabilityTextScale;
	}

	public void setDurabilityTextScale(float durabilityTextScale) {
		this.durabilityTextScale = Float.isFinite(durabilityTextScale)
				? Math.clamp(durabilityTextScale, MIN_DURABILITY_TEXT_SCALE, MAX_DURABILITY_TEXT_SCALE) : 0.5F;
	}

	public boolean isDurabilityTextShadow() {
		return durabilityTextShadow;
	}

	public void setDurabilityTextShadow(boolean durabilityTextShadow) {
		this.durabilityTextShadow = durabilityTextShadow;
	}

	public int getDurabilityTextColor() {
		return durabilityTextColor;
	}

	public void setDurabilityTextColor(int durabilityTextColor) {
		this.durabilityTextColor = durabilityTextColor;
	}

	public boolean isColorBasedDurabilityText() {
		return colorBasedDurabilityText;
	}

	public void setColorBasedDurabilityText(boolean colorBasedDurabilityText) {
		this.colorBasedDurabilityText = colorBasedDurabilityText;
	}

	public int getTextHealthyColor() {
		return textHealthyColor;
	}

	public void setTextHealthyColor(int textHealthyColor) {
		this.textHealthyColor = textHealthyColor;
	}

	public int getTextWarningColor() {
		return textWarningColor;
	}

	public void setTextWarningColor(int textWarningColor) {
		this.textWarningColor = textWarningColor;
	}

	public int getTextCriticalColor() {
		return textCriticalColor;
	}

	public void setTextCriticalColor(int textCriticalColor) {
		this.textCriticalColor = textCriticalColor;
	}

	public boolean isLowDurabilityWarningEnabled() {
		return lowDurabilityWarningEnabled;
	}

	public void setLowDurabilityWarningEnabled(boolean lowDurabilityWarningEnabled) {
		this.lowDurabilityWarningEnabled = lowDurabilityWarningEnabled;
	}

	public int getLowDurabilityThresholdPercent() {
		return lowDurabilityThresholdPercent;
	}

	public void setLowDurabilityThresholdPercent(int lowDurabilityThresholdPercent) {
		this.lowDurabilityThresholdPercent = Math.clamp(lowDurabilityThresholdPercent,
			MIN_LOW_DURABILITY_THRESHOLD_PERCENT, MAX_LOW_DURABILITY_THRESHOLD_PERCENT);
	}

	public ArmorHudWarningStyle getWarningStyle() {
		return warningStyle;
	}

	public void setWarningStyle(ArmorHudWarningStyle warningStyle) {
		this.warningStyle = warningStyle == null ? ArmorHudWarningStyle.PULSE : warningStyle;
	}

	public int getLowDurabilityWarningColor() {
		return lowDurabilityWarningColor;
	}

	public void setLowDurabilityWarningColor(int lowDurabilityWarningColor) {
		this.lowDurabilityWarningColor = lowDurabilityWarningColor;
	}

	private static ModuleLayout[] createDefaultIndividualLayouts() {
		return new ModuleLayout[] {
			new ModuleLayout(HudAnchor.CENTER_RIGHT, -8, -33),
			new ModuleLayout(HudAnchor.CENTER_RIGHT, -8, -11),
			new ModuleLayout(HudAnchor.CENTER_RIGHT, -8, 11),
			new ModuleLayout(HudAnchor.CENTER_RIGHT, -8, 33)
		};
	}

	private static ModuleLayout[] copyIndividualLayouts(ModuleLayout[] source) {
		ModuleLayout[] defaults = createDefaultIndividualLayouts();
		if (source == null) {
			return defaults;
		}
		for (int i = 0; i < Math.min(source.length, defaults.length); i++) {
			if (source[i] != null) {
				defaults[i].copyFrom(source[i]);
			}
		}
		return defaults;
	}

	private static ModuleLayout[] validatedIndividualLayouts(ModuleLayout[] source) {
		ModuleLayout[] layouts = copyIndividualLayouts(source);
		for (ModuleLayout layout : layouts) {
			layout.validate();
		}
		return layouts;
	}

	private static boolean[] createDefaultIndividualSlotVisibility() {
		return new boolean[] {true, true, true, true};
	}

	private static boolean[] copyIndividualSlotVisibility(boolean[] source) {
		boolean[] visibility = createDefaultIndividualSlotVisibility();
		if (source != null) {
			System.arraycopy(source, 0, visibility, 0, Math.min(source.length, visibility.length));
		}
		return visibility;
	}

	private static void requireArmorSlot(int slotIndex) {
		if (slotIndex < 0 || slotIndex >= ARMOR_SLOT_COUNT) {
			throw new IndexOutOfBoundsException("Unknown armor slot " + slotIndex);
		}
	}
}
