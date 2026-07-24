package net.davethemlgpro.client.module.armor;

import net.davethemlgpro.AnotherHUDMod;
import net.davethemlgpro.client.hud.HudBounds;
import net.davethemlgpro.client.hud.HudSize;
import net.davethemlgpro.client.module.HudModule;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

public final class ArmorHudModule implements HudModule<ArmorHudConfig> {
	public static final Identifier ID = AnotherHUDMod.id("armor");

	private static final Component DISPLAY_NAME = Component.translatable("module.another-hud-mod.armor");
	private static final EquipmentSlot[] ARMOR_SLOTS = {
			EquipmentSlot.HEAD,
			EquipmentSlot.CHEST,
			EquipmentSlot.LEGS,
			EquipmentSlot.FEET
	};
	private static final int ALL_SLOTS_MASK = (1 << ARMOR_SLOTS.length) - 1;
	private static final int ITEM_SIZE = 16;

	private int cachedMeasurementSignature = Integer.MIN_VALUE;
	private int cachedVisibleSlotsMask = -1;
	private HudSize cachedSize = new HudSize(0, 0);

	@Override
	public Identifier id() {
		return ID;
	}

	@Override
	public Component displayName() {
		return DISPLAY_NAME;
	}

	@Override
	public HudSize measure(Minecraft minecraft, ArmorHudConfig config) {
		int visibleSlotsMask = visibleSlotsMask(minecraft, config);
		int signature = measurementSignature(config);
		if (signature == cachedMeasurementSignature && visibleSlotsMask == cachedVisibleSlotsMask) {
			return cachedSize;
		}

		int visibleSlotCount = Integer.bitCount(visibleSlotsMask);
		if (visibleSlotCount == 0) {
			cachedSize = new HudSize(0, 0);
		} else {
			int slotSize = scaledItemSize(config);
			int spacing = scaledSpacing(config);
			int length = visibleSlotCount * slotSize + (visibleSlotCount - 1) * spacing;
			cachedSize = config.getOrientation() == ArmorHudOrientation.HORIZONTAL
					? new HudSize(length, slotSize)
					: new HudSize(slotSize, length);
		}

		cachedMeasurementSignature = signature;
		cachedVisibleSlotsMask = visibleSlotsMask;
		return cachedSize;
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Minecraft minecraft,
					   ArmorHudConfig config, HudBounds bounds) {
		if (minecraft.player == null || bounds.width() == 0 || bounds.height() == 0) {
			return;
		}

		int slotSize = scaledItemSize(config);
		int step = slotSize + scaledSpacing(config);
		int visibleIndex = 0;
		for (EquipmentSlot armorSlot : ARMOR_SLOTS) {
			ItemStack stack = minecraft.player.getItemBySlot(armorSlot);
			if (stack.isEmpty() && !config.isShowEmptySlots()) {
				continue;
			}

			int x = bounds.x();
			int y = bounds.y();
			if (config.getOrientation() == ArmorHudOrientation.HORIZONTAL) {
				x += visibleIndex * step;
			} else {
				y += visibleIndex * step;
			}

			if (stack.isEmpty()) {
				graphics.fill(x, y, x + slotSize, y + slotSize, config.getEmptySlotBackgroundColor());
			} else {
				drawScaledItem(graphics, stack, x, y, config.getScale());
				if (config.isDurabilityBarVisible()) {
					drawDurabilityBar(graphics, stack, x, y, slotSize, config);
				}
			}
			visibleIndex++;
		}
	}

	private int visibleSlotsMask(Minecraft minecraft, ArmorHudConfig config) {
		if (config.isShowEmptySlots()) {
			return ALL_SLOTS_MASK;
		}
		if (minecraft.player == null) {
			return 0;
		}

		int mask = 0;
		for (int i = 0; i < ARMOR_SLOTS.length; i++) {
			if (!minecraft.player.getItemBySlot(ARMOR_SLOTS[i]).isEmpty()) {
				mask |= 1 << i;
			}
		}
		return mask;
	}

	private int measurementSignature(ArmorHudConfig config) {
		int result = config.getOrientation().ordinal();
		result = 31 * result + config.getSpacing();
		result = 31 * result + Float.floatToIntBits(config.getScale());
		return result;
	}

	private int scaledItemSize(ArmorHudConfig config) {
		return Math.max(1, Math.round(ITEM_SIZE * config.getScale()));
	}

	private int scaledSpacing(ArmorHudConfig config) {
		return Math.max(0, Math.round(config.getSpacing() * config.getScale()));
	}

	private void drawScaledItem(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, float scale
	) {
		Matrix3x2fStack matrices = graphics.pose();
		matrices.pushMatrix();
		try {
			matrices.translate(x, y);
			matrices.scale(scale, scale);
			graphics.item(stack, 0, 0);
		} finally {
			matrices.popMatrix();
		}
	}

	private void drawDurabilityBar(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, int slotSize,
										  ArmorHudConfig config) {
		if (!stack.isDamageableItem()) {
			return;
		}

		int maxDamage = stack.getMaxDamage();
		if (maxDamage <= 0) {
			return;
		}
		float percent = Math.clamp((float) (maxDamage - stack.getDamageValue()) / maxDamage, 0.0F, 1.0F);
		int padding = Math.max(1, Math.round(config.getDurabilityBarHorizontalPadding() * config.getScale()));
		int barWidth = Math.max(1, slotSize - padding * 2);
		int barHeight = Math.max(1, Math.round(config.getDurabilityBarHeight() * config.getScale()));
		int barX = x + padding;
		int barY = y + slotSize - barHeight;
		int fillWidth = Math.round(barWidth * percent);

		graphics.fill(barX, barY,barX + barWidth,barY + barHeight,config.getDurabilityBackgroundColor());
		if (fillWidth > 0) {
			graphics.fill(barX, barY, barX + fillWidth, barY + Math.max(1, barHeight / 2),
				durabilityColor(percent, config));
		}
	}

	private int durabilityColor(float percent, ArmorHudConfig config) {
		if (percent >= 0.5F) {
			return interpolateColor(config.getDurabilityWarningColor(), config.getDurabilityHealthyColor(),
				(percent - 0.5F) * 2.0F);
		}
		return interpolateColor(config.getDurabilityCriticalColor(), config.getDurabilityWarningColor(),
			percent * 2.0F);
	}

	private int interpolateColor(int start, int end, float progress) {
		float amount = Math.clamp(progress, 0.0F, 1.0F);
		int alpha = interpolateChannel(start >>> 24, end >>> 24, amount);
		int red = interpolateChannel(start >>> 16 & 0xFF, end >>> 16 & 0xFF, amount);
		int green = interpolateChannel(start >>> 8 & 0xFF, end >>> 8 & 0xFF, amount);
		int blue = interpolateChannel(start & 0xFF, end & 0xFF, amount);
		return alpha << 24 | red << 16 | green << 8 | blue;
	}

	private int interpolateChannel(int start, int end, float progress) {
		return Math.round(start + (end - start) * progress);
	}
}
