package net.davethemlgpro.client.module.armor;

import net.davethemlgpro.client.hud.HudSize;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

final class ArmorHudLayout {
	private static final EquipmentSlot[] ARMOR_SLOTS = {
			EquipmentSlot.HEAD,
			EquipmentSlot.CHEST,
			EquipmentSlot.LEGS,
			EquipmentSlot.FEET
	};
	private static final int ITEM_SIZE = 16;
	private static final String INFINITE_DURABILITY = "∞";
	private static final float INFINITE_DURABILITY_TEXT_SCALE = 1.25F;

	private final ArmorHudSlotLayout[] slots = {
			new ArmorHudSlotLayout(),
			new ArmorHudSlotLayout(),
			new ArmorHudSlotLayout(),
			new ArmorHudSlotLayout()
	};
	private final int[] entryWidths = new int[ARMOR_SLOTS.length];
	private final int[] entryHeights = new int[ARMOR_SLOTS.length];
	private final int[] itemOffsetsX = new int[ARMOR_SLOTS.length];
	private final int[] itemOffsetsY = new int[ARMOR_SLOTS.length];
	private final int[] textOffsetsX = new int[ARMOR_SLOTS.length];
	private final int[] textOffsetsY = new int[ARMOR_SLOTS.length];
	private final int[] textWidths = new int[ARMOR_SLOTS.length];
	private final int[] textHeights = new int[ARMOR_SLOTS.length];
	private final String[] durabilityTexts = new String[ARMOR_SLOTS.length];

	private HudSize size = new HudSize(0, 0);

	public HudSize recalculate(Minecraft minecraft, ArmorHudConfig config) {
		int itemSize = scaledItemSize(config);
		int spacing = scaledSpacing(config);
		int textGap = Math.max(1, Math.round(config.getScale()));
		int visibleCount = 0;
		int groupWidth = 0;
		int groupHeight = 0;

		for (int i = 0; i < ARMOR_SLOTS.length; i++) {
			ItemStack stack = minecraft.player == null
					? ItemStack.EMPTY
					: minecraft.player.getItemBySlot(ARMOR_SLOTS[i]);
			boolean visible = config.isShowEmptySlots() || !stack.isEmpty();
			if (!visible) {
				slots[i].hide();
				continue;
			}

			String text = durabilityText(stack, config);
			float renderScale = durabilityTextRenderScale(config, text);
			int textWidth = text.isEmpty() ? 0 : (int) Math.ceil(minecraft.font.width(text) * renderScale);
			int textHeight = text.isEmpty() ? 0 : (int) Math.ceil(minecraft.font.lineHeight * renderScale);
			calculateEntrySize(i, itemSize, textWidth, textHeight, textGap, config.getTextPosition(),
				config.isDurabilityBarVisible(), !text.isEmpty());
			textWidths[i] = textWidth;
			textHeights[i] = textHeight;
			durabilityTexts[i] = text;

			if (config.getOrientation() == ArmorHudOrientation.HORIZONTAL) {
				groupWidth += entryWidths[i];
				groupHeight = Math.max(groupHeight, entryHeights[i]);
			} else {
				groupWidth = Math.max(groupWidth, entryWidths[i]);
				groupHeight += entryHeights[i];
			}
			visibleCount++;
		}

		if (visibleCount == 0) {
			size = new HudSize(0, 0);
			return size;
		}

		int totalSpacing = (visibleCount - 1) * spacing;
		if (config.getOrientation() == ArmorHudOrientation.HORIZONTAL) {
			groupWidth += totalSpacing;
		} else {
			groupHeight += totalSpacing;
		}

		int visualOutset = slotVisualOutset(config);
		int cursor = 0;
		for (int i = 0; i < ARMOR_SLOTS.length; i++) {
			if (!config.isShowEmptySlots() && minecraft.player != null
					&& minecraft.player.getItemBySlot(ARMOR_SLOTS[i]).isEmpty()) {
				continue;
			}
			if (minecraft.player == null && !config.isShowEmptySlots()) {
				continue;
			}

			int entryX;
			int entryY = 0;
			if (config.getOrientation() == ArmorHudOrientation.HORIZONTAL) {
				entryX = cursor;
				cursor += entryWidths[i] + spacing;
			} else {
				entryY = cursor;
				entryX = verticalEntryOffset(config.getTextPosition(), groupWidth, entryWidths[i]);
				cursor += entryHeights[i] + spacing;
			}

			slots[i].setEntry(visualOutset + entryX + itemOffsetsX[i], visualOutset + entryY + itemOffsetsY[i], itemSize,
				visualOutset + entryX + textOffsetsX[i], visualOutset + entryY + textOffsetsY[i], textWidths[i], textHeights[i], durabilityTexts[i]);
		}

		size = new HudSize(groupWidth + visualOutset * 2, groupHeight + visualOutset * 2);
		return size;
	}

	public ArmorHudSlotLayout getSlot(int index) {
		return slots[index];
	}

	public HudSize getSize() {
		return size;
	}

	public static int getSlotCount() {
		return ARMOR_SLOTS.length;
	}

	public static EquipmentSlot getEquipmentSlot(int index) {
		return ARMOR_SLOTS[index];
	}

	public static float durabilityTextRenderScale(ArmorHudConfig config, String text) {
		float scale = config.getScale() * config.getDurabilityTextScale();
		return INFINITE_DURABILITY.equals(text) ? scale * INFINITE_DURABILITY_TEXT_SCALE : scale;
	}

	public static float durabilityPercent(ItemStack stack) {
		if (!stack.isDamageableItem()) {
			return 1.0F;
		}
		int maxDamage = stack.getMaxDamage();
		if (maxDamage <= 0) {
			return 1.0F;
		}
		return Math.clamp((float) (maxDamage - stack.getDamageValue()) / maxDamage, 0.0F, 1.0F);
	}

	public static boolean isUnbreakableItem(ItemStack stack) {
		return stack.getMaxDamage() > 0 && stack.has(DataComponents.UNBREAKABLE);
	}

	private String durabilityText(ItemStack stack, ArmorHudConfig config) {
		boolean unbreakable = isUnbreakableItem(stack);
		if ((!stack.isDamageableItem() && !unbreakable) || config.getDurabilityMode() == ArmorHudDurabilityMode.NONE) {
			return "";
		}
		if (unbreakable) {
			return INFINITE_DURABILITY;
		}
		return switch (config.getDurabilityMode()) {
			case VALUE -> Integer.toString(stack.getMaxDamage() - stack.getDamageValue());
			case PERCENT -> Math.round(durabilityPercent(stack) * 100.0F) + "%";
			case NONE -> "";
		};
	}

	private void calculateEntrySize( int index, int itemSize, int textWidth, int textHeight, int textGap,
									 ArmorHudTextPosition position, boolean barEnabled, boolean textVisible) {
		if (!textVisible) {
			setEntrySize(index, itemSize, itemSize, 0, 0, 0, 0);
			return;
		}

		switch (position) {
			case CENTER -> setEntrySize(index, itemSize, itemSize, 0, 0,
				(itemSize - textWidth) / 2, (itemSize - textHeight) / 2);
			case TOP -> setEntrySize( index, itemSize, itemSize, 0, 0,
				(itemSize - textWidth) / 2, 0);
			case BOTTOM -> {
				if (!barEnabled) {
					setEntrySize(index, itemSize, itemSize, 0, 0,
						(itemSize - textWidth) / 2, itemSize - textHeight);
				} else {
					int width = Math.max(itemSize, textWidth);
					setEntrySize(index, width, itemSize + textGap + textHeight, (width - itemSize) / 2,
						0, (width - textWidth) / 2, itemSize + textGap);
				}
			}
			case LEFT -> {
				int height = Math.max(itemSize, textHeight);
				setEntrySize(index, textWidth + textGap + itemSize, height, textWidth + textGap,
					(height - itemSize) / 2, 0, (height - textHeight) / 2);
			}
			case RIGHT -> {
				int height = Math.max(itemSize, textHeight);
				setEntrySize(index, itemSize + textGap + textWidth, height, 0,
					(height - itemSize) / 2, itemSize + textGap, (height - textHeight) / 2);
			}
		}
	}

	private void setEntrySize(int index, int width, int height, int itemOffsetX,
							  int itemOffsetY, int textOffsetX, int textOffsetY) {
		entryWidths[index] = width;
		entryHeights[index] = height;
		itemOffsetsX[index] = itemOffsetX;
		itemOffsetsY[index] = itemOffsetY;
		textOffsetsX[index] = textOffsetX;
		textOffsetsY[index] = textOffsetY;
	}

	private int verticalEntryOffset(ArmorHudTextPosition position, int groupWidth, int entryWidth) {
		return switch (position) {
			case LEFT -> groupWidth - entryWidth;
			case RIGHT -> 0;
			case TOP, BOTTOM, CENTER -> (groupWidth - entryWidth) / 2;
		};
	}

	private int scaledItemSize(ArmorHudConfig config) {
		return Math.max(1, Math.round(ITEM_SIZE * config.getScale()));
	}

	private int scaledSpacing(ArmorHudConfig config) {
		return Math.max(0, Math.round(config.getSpacing() * config.getScale()));
	}

	private int slotVisualOutset(ArmorHudConfig config) {
		return switch (config.getSlotStyle()) {
			case CLEAR -> 0;
			case INVENTORY -> 1;
			case HOTBAR -> Math.max(1, Math.round(3.0F * config.getScale()));
		};
	}
}
