package net.davethemlgpro.client.module.miningsession;

import com.google.gson.annotations.SerializedName;
import net.davethemlgpro.client.hud.layout.HudAnchor;
import net.davethemlgpro.client.hud.layout.ModuleLayout;
import net.davethemlgpro.client.module.HudModuleConfig;
import net.minecraft.world.item.ItemStack;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public final class MiningSessionHudConfig implements HudModuleConfig<MiningSessionHudConfig> {
	public static final int MIN_VISIBLE_BLOCKS = 1;
	public static final int MAX_VISIBLE_BLOCKS = 10;
	public static final int MAX_TRACKED_ITEMS = 10;
	public static final int MAX_ITEM_GOALS = 15;
	public static final int MAX_GOAL_ITEM_AMOUNT = 1_000_000_000;
	public static final int MIN_VISIBLE_GOALS = 1;
	public static final int MAX_VISIBLE_GOALS = 10;
	public static final double MIN_UI_SCALE = 0.5D;
	public static final double MAX_UI_SCALE = 2.0D;
	public static final int MIN_ROW_SPACING = 0;
	public static final int MAX_ROW_SPACING = 8;
	public static final int DEFAULT_BACKGROUND_COLOR = 0x73101010;
	public static final int DEFAULT_TEXT_COLOR = 0xFFFFFFFF;
	public static final int DEFAULT_ACCENT_COLOR = 0xFF55FF55;
	public static final BigDecimal MAX_ITEM_VALUE = new BigDecimal("1000000000000.00");
	public static final int ITEM_VALUE_SCALE = 2;

	@SerializedName("enabled")
	private boolean visible = true;
	private ModuleLayout layout = new ModuleLayout(HudAnchor.TOP_LEFT, 8, 8);
	private MiningSessionRowStyle rowStyle = MiningSessionRowStyle.ICON_NAME_AMOUNT;
	private int maxVisibleBlocks = 6;
	private double uiScale = 1.0D;
	private int rowSpacing = 1;
	private boolean autoStartOnBlockBreak;
	private boolean oresOnly = true;
	private MiningSessionInventoryOrder inventoryOrder = MiningSessionInventoryOrder.CUSTOM;
	private List<MiningSessionTrackedItem> trackedInventoryEntries = new ArrayList<>();
	private boolean showStatisticsFooter;
	private boolean showFooterBlocks = true;
	private boolean showFooterItems = true;
	private boolean showFooterValue;
	private MiningSessionGoalMode goalMode = MiningSessionGoalMode.NONE;
	private int maxVisibleGoals = 3;
	private List<MiningSessionItemGoal> itemGoals = new ArrayList<>();
	private BigDecimal valueGoal = BigDecimal.ZERO;
	private int backgroundColor = DEFAULT_BACKGROUND_COLOR;
	private int textColor = DEFAULT_TEXT_COLOR;
	private int accentColor = DEFAULT_ACCENT_COLOR;

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
	public MiningSessionHudConfig copy() {
		MiningSessionHudConfig copy = new MiningSessionHudConfig();
		copy.copyFrom(this);
		return copy;
	}

	@Override
	public void copyFrom(MiningSessionHudConfig source) {
		visible = source.visible;
		layout = new ModuleLayout();
		layout.copyFrom(source.layout);
		rowStyle = source.rowStyle;
		maxVisibleBlocks = source.maxVisibleBlocks;
		uiScale = source.uiScale;
		rowSpacing = source.rowSpacing;
		autoStartOnBlockBreak = source.autoStartOnBlockBreak;
		oresOnly = source.oresOnly;
		inventoryOrder = source.inventoryOrder;
		trackedInventoryEntries = new ArrayList<>();
		if (source.trackedInventoryEntries != null) {
			for (MiningSessionTrackedItem entry : source.trackedInventoryEntries) {
				if (entry != null) {
					trackedInventoryEntries.add(entry.copy());
				}
			}
		}
		showStatisticsFooter = source.showStatisticsFooter;
		showFooterBlocks = source.showFooterBlocks;
		showFooterItems = source.showFooterItems;
		showFooterValue = source.showFooterValue;
		goalMode = source.goalMode;
		maxVisibleGoals = source.maxVisibleGoals;
		itemGoals = new ArrayList<>();
		if (source.itemGoals != null) {
			for (MiningSessionItemGoal goal : source.itemGoals) {
				if (goal != null) {
					itemGoals.add(goal.copy());
				}
			}
		}
		valueGoal = source.valueGoal;
		backgroundColor = source.backgroundColor;
		textColor = source.textColor;
		accentColor = source.accentColor;
		validate();
	}

	@Override
	public void validate() {
		if (layout == null) {
			layout = new ModuleLayout(HudAnchor.TOP_LEFT, 8, 8);
		}
		layout.validate();
		if (rowStyle == null) {
			rowStyle = MiningSessionRowStyle.ICON_NAME_AMOUNT;
		}
		if (inventoryOrder == null) {
			inventoryOrder = MiningSessionInventoryOrder.CUSTOM;
		}
		if (goalMode == null) {
			goalMode = MiningSessionGoalMode.NONE;
		}
		maxVisibleBlocks = Math.clamp(maxVisibleBlocks, MIN_VISIBLE_BLOCKS, MAX_VISIBLE_BLOCKS);
		uiScale = Math.clamp(finiteOrDefault(uiScale, 1.0D), MIN_UI_SCALE, MAX_UI_SCALE);
		rowSpacing = Math.clamp(rowSpacing, MIN_ROW_SPACING, MAX_ROW_SPACING);
		maxVisibleGoals = Math.clamp(maxVisibleGoals, MIN_VISIBLE_GOALS, MAX_VISIBLE_GOALS);
		List<MiningSessionTrackedItem> repairedEntries = new ArrayList<>();
		LinkedHashSet<String> identities = new LinkedHashSet<>();
		if (trackedInventoryEntries != null) {
			for (MiningSessionTrackedItem entry : trackedInventoryEntries) {
				if (entry != null && entry.validate() && identities.add(entry.identityKey())
					&& repairedEntries.size() < MAX_TRACKED_ITEMS) {
					repairedEntries.add(entry);
				}
			}
		}
		trackedInventoryEntries = repairedEntries;
		List<MiningSessionItemGoal> repairedGoals = new ArrayList<>();
		LinkedHashSet<String> goalItems = new LinkedHashSet<>();
		if (itemGoals != null) {
			for (MiningSessionItemGoal goal : itemGoals) {
				if (goal != null && goal.validate() && goalItems.add(goal.itemId())
					&& repairedGoals.size() < MAX_ITEM_GOALS) {
					repairedGoals.add(goal);
				}
			}
		}
		itemGoals = repairedGoals;
		valueGoal = sanitizeItemValue(valueGoal);
	}

	public MiningSessionRowStyle getRowStyle() { return rowStyle; }
	public void setRowStyle(MiningSessionRowStyle rowStyle) {
		this.rowStyle = rowStyle == null ? MiningSessionRowStyle.ICON_NAME_AMOUNT : rowStyle;
	}
	public int getMaxVisibleBlocks() { return maxVisibleBlocks; }
	public void setMaxVisibleBlocks(int value) {
		maxVisibleBlocks = Math.clamp(value, MIN_VISIBLE_BLOCKS, MAX_VISIBLE_BLOCKS);
	}
	public double getUiScale() { return uiScale; }
	public void setUiScale(double value) {
		uiScale = Math.clamp(finiteOrDefault(value, 1.0D), MIN_UI_SCALE, MAX_UI_SCALE);
	}
	public int getRowSpacing() { return rowSpacing; }
	public void setRowSpacing(int value) {
		rowSpacing = Math.clamp(value, MIN_ROW_SPACING, MAX_ROW_SPACING);
	}
	public boolean isAutoStartOnBlockBreak() { return autoStartOnBlockBreak; }
	public void setAutoStartOnBlockBreak(boolean value) { autoStartOnBlockBreak = value; }
	public boolean isOresOnly() { return oresOnly; }
	public void setOresOnly(boolean value) { oresOnly = value; }
	public MiningSessionInventoryOrder getInventoryOrder() { return inventoryOrder; }
	public void setInventoryOrder(MiningSessionInventoryOrder value) {
		inventoryOrder = value == null ? MiningSessionInventoryOrder.CUSTOM : value;
	}
	public List<String> getTrackedInventoryItems() {
		return trackedInventoryEntries.stream().map(MiningSessionTrackedItem::itemId).toList();
	}
	List<MiningSessionTrackedItem> getTrackedInventoryEntries() {
		return List.copyOf(trackedInventoryEntries);
	}
	public boolean addTrackedInventoryItem(String itemId) {
		return putTrackedInventoryItem(itemId, BigDecimal.ZERO);
	}
	public boolean putTrackedInventoryItem(String itemId, BigDecimal value) {
		String normalized = normalizeItemId(itemId);
		if (normalized == null) {
			return false;
		}
		for (MiningSessionTrackedItem entry : trackedInventoryEntries) {
			if (entry.itemId().equals(normalized) && entry.displayName() == null && entry.appearance() == null) {
				entry.setUnitValue(value);
				return true;
			}
		}
		if (trackedInventoryEntries.size() >= MAX_TRACKED_ITEMS) {
			return false;
		}
		trackedInventoryEntries.add(new MiningSessionTrackedItem(normalized, value, null));
		return true;
	}
	public boolean putHeldTrackedInventoryItem(ItemStack stack, BigDecimal value) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}
		String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM
			.getKey(stack.getItem()).toString();
		for (MiningSessionTrackedItem entry : trackedInventoryEntries) {
			if (entry.matches(stack)) {
				entry.setUnitValue(value);
				return true;
			}
		}
		if (trackedInventoryEntries.size() >= MAX_TRACKED_ITEMS) {
			return false;
		}
		MiningSessionTrackedItem entry = new MiningSessionTrackedItem(itemId, value,
			MiningSessionItemAppearance.capture(stack));
		entry.setDisplayName(MiningSessionTrackedItem.customName(stack));
		trackedInventoryEntries.add(entry);
		return true;
	}
	public void removeTrackedInventoryItem(int index) {
		if (index >= 0 && index < trackedInventoryEntries.size()) {
			trackedInventoryEntries.remove(index);
		}
	}
	public boolean moveTrackedInventoryItem(int fromIndex, int toIndex) {
		if (fromIndex < 0 || fromIndex >= trackedInventoryEntries.size()
			|| toIndex < 0 || toIndex >= trackedInventoryEntries.size() || fromIndex == toIndex) {
			return false;
		}
		MiningSessionTrackedItem entry = trackedInventoryEntries.remove(fromIndex);
		trackedInventoryEntries.add(toIndex, entry);
		return true;
	}
	public void setTrackedInventoryItemValue(int index, BigDecimal value) {
		if (index >= 0 && index < trackedInventoryEntries.size()) {
			trackedInventoryEntries.get(index).setUnitValue(value);
		}
	}
	public BigDecimal getTrackedInventoryItemValue(String itemId) {
		String normalized = normalizeItemId(itemId);
		return trackedInventoryEntries.stream()
			.filter(entry -> entry.itemId().equals(normalized))
			.map(MiningSessionTrackedItem::unitValue).findFirst().orElse(BigDecimal.ZERO);
	}
	public void setTrackedInventoryItemValue(String itemId, BigDecimal value) {
		String normalized = normalizeItemId(itemId);
		for (MiningSessionTrackedItem entry : trackedInventoryEntries) {
			if (entry.itemId().equals(normalized)) {
				entry.setUnitValue(value);
				return;
			}
		}
	}
	public boolean isShowStatisticsFooter() { return showStatisticsFooter; }
	public void setShowStatisticsFooter(boolean value) { showStatisticsFooter = value; }
	public boolean isShowFooterBlocks() { return showFooterBlocks; }
	public void setShowFooterBlocks(boolean value) { showFooterBlocks = value; }
	public boolean isShowFooterItems() { return showFooterItems; }
	public void setShowFooterItems(boolean value) { showFooterItems = value; }
	public boolean isShowFooterValue() { return showFooterValue; }
	public void setShowFooterValue(boolean value) { showFooterValue = value; }
	public MiningSessionGoalMode getGoalMode() { return goalMode; }
	public void setGoalMode(MiningSessionGoalMode value) {
		goalMode = value == null ? MiningSessionGoalMode.NONE : value;
	}
	public int getMaxVisibleGoals() { return maxVisibleGoals; }
	public void setMaxVisibleGoals(int value) {
		maxVisibleGoals = Math.clamp(value, MIN_VISIBLE_GOALS, MAX_VISIBLE_GOALS);
	}
	public List<String> getItemGoalIds() {
		return itemGoals.stream().map(MiningSessionItemGoal::itemId).toList();
	}
	List<MiningSessionItemGoal> getItemGoals() { return List.copyOf(itemGoals); }
	public boolean putItemGoal(String itemId, int amount) {
		String normalized = normalizeItemId(itemId);
		if (normalized == null || amount < 1) {
			return false;
		}
		for (MiningSessionItemGoal goal : itemGoals) {
			if (goal.itemId().equals(normalized)) {
				goal.setTargetAmount(amount);
				return true;
			}
		}
		if (itemGoals.size() >= MAX_ITEM_GOALS) {
			return false;
		}
		itemGoals.add(new MiningSessionItemGoal(normalized, amount));
		return true;
	}
	public void removeItemGoal(int index) {
		if (index >= 0 && index < itemGoals.size()) {
			itemGoals.remove(index);
		}
	}
	public BigDecimal getValueGoal() { return valueGoal; }
	public void setValueGoal(BigDecimal value) { valueGoal = sanitizeItemValue(value); }
	public int getBackgroundColor() { return backgroundColor; }
	public void setBackgroundColor(int value) { backgroundColor = value; }
	public int getTextColor() { return textColor; }
	public void setTextColor(int value) { textColor = value; }
	public int getAccentColor() { return accentColor; }
	public void setAccentColor(int value) { accentColor = value; }

	static String normalizeItemId(String itemId) {
		if (itemId == null) {
			return null;
		}
		String normalized = itemId.trim().toLowerCase(Locale.ROOT);
		if (!normalized.contains(":")) {
			normalized = "minecraft:" + normalized;
		}
		return normalized.matches("[a-z0-9_.-]+:[a-z0-9/._-]+") ? normalized : null;
	}

	static BigDecimal sanitizeItemValue(BigDecimal value) {
		if (value == null || value.signum() < 0) {
			return BigDecimal.ZERO;
		}
		return value.min(MAX_ITEM_VALUE).setScale(ITEM_VALUE_SCALE, java.math.RoundingMode.HALF_UP)
			.stripTrailingZeros();
	}

	private static double finiteOrDefault(double value, double fallback) {
		return Double.isFinite(value) ? value : fallback;
	}
}
