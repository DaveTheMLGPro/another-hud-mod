package net.davethemlgpro.client.module.miningsession;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.math.BigDecimal;

final class MiningSessionTrackedItem {
	private static final int MAX_DISPLAY_NAME_LENGTH = 256;

	private String itemId;
	private BigDecimal unitValue = BigDecimal.ZERO;
	private MiningSessionItemAppearance appearance;
	private String displayName;

	MiningSessionTrackedItem() {
	}

	MiningSessionTrackedItem(String itemId, BigDecimal unitValue, MiningSessionItemAppearance appearance) {
		this.itemId = itemId;
		this.unitValue = unitValue;
		this.appearance = appearance == null ? null : appearance.copy();
		validate();
	}

	MiningSessionTrackedItem copy() {
		MiningSessionTrackedItem copy = new MiningSessionTrackedItem(itemId, unitValue, appearance);
		copy.displayName = displayName;
		return copy;
	}

	boolean validate() {
		itemId = MiningSessionHudConfig.normalizeItemId(itemId);
		unitValue = MiningSessionHudConfig.sanitizeItemValue(unitValue);
		if (appearance != null) {
			appearance.validate();
			if (!appearance.isMeaningful()) {
				appearance = null;
			}
		}
		displayName = sanitizeDisplayName(displayName);
		return itemId != null;
	}

	String itemId() {
		return itemId;
	}

	BigDecimal unitValue() {
		return unitValue;
	}

	void setUnitValue(BigDecimal value) {
		unitValue = MiningSessionHudConfig.sanitizeItemValue(value);
	}

	String displayName() {
		return displayName;
	}

	void setDisplayName(String value) {
		displayName = sanitizeDisplayName(value);
	}

	MiningSessionItemAppearance appearance() {
		return appearance == null ? null : appearance.copy();
	}

	boolean matches(ItemStack stack) {
		if (stack.isEmpty() || !BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(itemId)) {
			return false;
		}
		if (!java.util.Objects.equals(displayName, customName(stack))) {
			return false;
		}
		return appearance == null
			? !MiningSessionItemAppearance.hasMeaningfulAppearance(stack)
			: appearance.matches(stack);
	}

	void applyAppearance(ItemStack stack) {
		if (appearance != null) {
			appearance.applyTo(stack);
		}
	}

	String identityKey() {
		String nameKey = displayName == null ? "base" : displayName.length() + ":" + displayName;
		return itemId + "|name=" + nameKey
			+ "|model=" + (appearance == null ? "base" : appearance.identityKey());
	}

	static String customName(ItemStack stack) {
		Component customName = stack.get(DataComponents.CUSTOM_NAME);
		return customName == null ? null : sanitizeDisplayName(customName.getString());
	}

	private static String sanitizeDisplayName(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.substring(0, Math.min(trimmed.length(), MAX_DISPLAY_NAME_LENGTH));
	}
}
