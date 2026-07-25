package net.davethemlgpro.client.module.armor;

import net.davethemlgpro.AnotherHUDMod;
import net.davethemlgpro.client.hud.HudBounds;
import net.davethemlgpro.client.hud.HudSize;
import net.davethemlgpro.client.module.HudModule;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

public final class ArmorHudModule implements HudModule<ArmorHudConfig> {
	public static final Identifier ID = AnotherHUDMod.id("armor");

	private static final Component DISPLAY_NAME = Component.translatable("module.another-hud-mod.armor");
	private static final Identifier INVENTORY_SLOT_SPRITE = Identifier.withDefaultNamespace("container/slot");
	private static final Identifier HOTBAR_SLOT_TEXTURE = AnotherHUDMod.id("textures/hotbar_icon.png");
	private static final Identifier[] EMPTY_ARMOR_ICONS = {
		AnotherHUDMod.id("textures/empty_helmet_icon.png"),
		AnotherHUDMod.id("textures/empty_chestplate_icon.png"),
		AnotherHUDMod.id("textures/empty_leggings_icon.png"),
		AnotherHUDMod.id("textures/empty_boots_icon.png")
	};
	private static final int EMPTY_ARMOR_ICON_SIZE = 16;
	private static final int HOTBAR_SLOT_TEXTURE_SIZE = 22;

	private final ArmorHudLayout layout = new ArmorHudLayout();
	private final boolean[] cachedEmptySlots = new boolean[ArmorHudLayout.getSlotCount()];
	private final boolean[] cachedUnbreakableSlots = new boolean[ArmorHudLayout.getSlotCount()];
	private final int[] cachedMaxDamage = new int[ArmorHudLayout.getSlotCount()];
	private final int[] cachedDamage = new int[ArmorHudLayout.getSlotCount()];

	private boolean layoutCacheInitialized;
	private ArmorHudOrientation cachedOrientation;
	private ArmorHudDurabilityMode cachedDurabilityMode;
	private ArmorHudTextPosition cachedTextPosition;
	private int cachedSpacing;
	private float cachedScale;
	private float cachedDurabilityTextScale;
	private boolean cachedShowEmptySlots;
	private boolean cachedDurabilityBarVisible;
	private boolean cachedLowDurabilityWarningEnabled;
	private ArmorHudSlotStyle cachedSlotStyle;

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
		if (!isLayoutCacheValid(minecraft, config)) {
			HudSize size = layout.recalculate(minecraft, config);
			captureLayoutState(minecraft, config);
			return size;
		}
		return layout.getSize();
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Minecraft minecraft, ArmorHudConfig config, HudBounds bounds) {
		if (minecraft.player == null || bounds.width() == 0 || bounds.height() == 0) {
			return;
		}

		float warningOpacity = -1.0F;
		for (int i = 0; i < ArmorHudLayout.getSlotCount(); i++) {
			ArmorHudSlotLayout slot = layout.getSlot(i);
			if (!slot.isVisible()) {
				continue;
			}

			ItemStack stack = minecraft.player.getItemBySlot(ArmorHudLayout.getEquipmentSlot(i));
			int itemX = bounds.x() + slot.getItemX();
			int itemY = bounds.y() + slot.getItemY();
			drawSlotBackground(graphics, itemX, itemY, slot.getItemSize(), config);
			if (stack.isEmpty()) {
				drawScaledEmptyArmorIcon(graphics, i, itemX, itemY, config.getScale());
				continue;
			}

			drawScaledItem(graphics, stack, itemX, itemY, config.getScale());
			if (config.isDurabilityBarVisible()) {
				drawDurabilityBar(graphics, stack, itemX, itemY, slot.getItemSize(), config);
			}
			if (isLowDurability(stack, config)) {
				if (warningOpacity < 0.0F) {
					warningOpacity = warningOpacity(config.getWarningStyle());
				}
				drawLowDurabilityWarning(graphics, itemX, itemY, slot.getItemSize(),
					config.getLowDurabilityWarningColor(), warningOpacity);
			}
			drawDurabilityText(graphics, minecraft, stack, slot, bounds, config);
		}
	}

	private boolean isLayoutCacheValid(Minecraft minecraft, ArmorHudConfig config) {
		if (!layoutCacheInitialized
				|| cachedOrientation != config.getOrientation()
				|| cachedDurabilityMode != config.getDurabilityMode()
				|| cachedTextPosition != config.getTextPosition()
				|| cachedSpacing != config.getSpacing()
				|| Float.compare(cachedScale, config.getScale()) != 0
				|| Float.compare(cachedDurabilityTextScale, config.getDurabilityTextScale()) != 0
				|| cachedShowEmptySlots != config.isShowEmptySlots()
				|| cachedDurabilityBarVisible != config.isDurabilityBarVisible()
				|| cachedLowDurabilityWarningEnabled != config.isLowDurabilityWarningEnabled()
				|| cachedSlotStyle != config.getSlotStyle()) {
			return false;
		}

		for (int i = 0; i < ArmorHudLayout.getSlotCount(); i++) {
			ItemStack stack = minecraft.player == null ? ItemStack.EMPTY
					: minecraft.player.getItemBySlot(ArmorHudLayout.getEquipmentSlot(i));
			if (cachedEmptySlots[i] != stack.isEmpty() || cachedMaxDamage[i] != stack.getMaxDamage()
				|| cachedDamage[i] != stack.getDamageValue()
				|| cachedUnbreakableSlots[i] != ArmorHudLayout.isUnbreakableItem(stack)) {
				return false;
			}
		}
		return true;
	}

	private void captureLayoutState(Minecraft minecraft, ArmorHudConfig config) {
		cachedOrientation = config.getOrientation();
		cachedDurabilityMode = config.getDurabilityMode();
		cachedTextPosition = config.getTextPosition();
		cachedSpacing = config.getSpacing();
		cachedScale = config.getScale();
		cachedDurabilityTextScale = config.getDurabilityTextScale();
		cachedShowEmptySlots = config.isShowEmptySlots();
		cachedDurabilityBarVisible = config.isDurabilityBarVisible();
		cachedLowDurabilityWarningEnabled = config.isLowDurabilityWarningEnabled();
		cachedSlotStyle = config.getSlotStyle();

		for (int i = 0; i < ArmorHudLayout.getSlotCount(); i++) {
			ItemStack stack = minecraft.player == null ? ItemStack.EMPTY
					: minecraft.player.getItemBySlot(ArmorHudLayout.getEquipmentSlot(i));
			cachedEmptySlots[i] = stack.isEmpty();
			cachedMaxDamage[i] = stack.getMaxDamage();
			cachedDamage[i] = stack.getDamageValue();
			cachedUnbreakableSlots[i] = ArmorHudLayout.isUnbreakableItem(stack);
		}
		layoutCacheInitialized = true;
	}

	private void drawSlotBackground(GuiGraphicsExtractor graphics, int x, int y, int slotSize,
	                                ArmorHudConfig config) {
		switch (config.getSlotStyle()) {
			case CLEAR -> {
			}
			case INVENTORY -> graphics.blitSprite(RenderPipelines.GUI_TEXTURED, INVENTORY_SLOT_SPRITE,
				x - 1, y - 1, slotSize + 2, slotSize + 2);
			case HOTBAR -> drawHotbarSlotBackground(graphics, x, y, config.getScale());
		}
	}

	private void drawHotbarSlotBackground(GuiGraphicsExtractor graphics, int x, int y, float scale) {
		Matrix3x2fStack matrices = graphics.pose();
		matrices.pushMatrix();
		try {
			matrices.translate(x - 3.0F * scale, y - 3.0F * scale);
			matrices.scale(scale, scale);
			graphics.blit(RenderPipelines.GUI_TEXTURED, HOTBAR_SLOT_TEXTURE, 0, 0, 0.0F, 0.0F,
				HOTBAR_SLOT_TEXTURE_SIZE, HOTBAR_SLOT_TEXTURE_SIZE,
				HOTBAR_SLOT_TEXTURE_SIZE, HOTBAR_SLOT_TEXTURE_SIZE);
		} finally {
			matrices.popMatrix();
		}
	}

	private void drawScaledEmptyArmorIcon(GuiGraphicsExtractor graphics, int slotIndex, int x, int y, float scale) {
		Matrix3x2fStack matrices = graphics.pose();
		matrices.pushMatrix();
		try {
			matrices.translate(x, y);
			matrices.scale(scale, scale);
			graphics.blit(RenderPipelines.GUI_TEXTURED, EMPTY_ARMOR_ICONS[slotIndex], 0, 0, 0.0F, 0.0F,
				EMPTY_ARMOR_ICON_SIZE, EMPTY_ARMOR_ICON_SIZE, EMPTY_ARMOR_ICON_SIZE, EMPTY_ARMOR_ICON_SIZE);
		} finally {
			matrices.popMatrix();
		}
	}

	private boolean isLowDurability(ItemStack stack, ArmorHudConfig config) {
		return config.isLowDurabilityWarningEnabled() && stack.isDamageableItem()
			&& !ArmorHudLayout.isUnbreakableItem(stack)
			&& ArmorHudLayout.durabilityPercent(stack) * 100.0F <= config.getLowDurabilityThresholdPercent();
	}

	private float warningOpacity(ArmorHudWarningStyle style) {
		double time = System.nanoTime() / 1_000_000_000.0D;
		return switch (style) {
			case COLOR -> 1.0F;
			case PULSE -> 0.35F + 0.65F * ((float) Math.sin(time * 4.0D) + 1.0F) / 2.0F;
			case FLASH -> Math.sin(time * 6.0D) >= 0.0D ? 1.0F : 0.0F;
		};
	}

	private void drawLowDurabilityWarning(GuiGraphicsExtractor graphics, int x, int y, int slotSize,
	                                      int warningColor, float opacity) {
		if (opacity <= 0.0F) {
			return;
		}

		int color = withAlpha(warningColor, opacity);
		graphics.outline(x - 1, y - 1, slotSize + 2, slotSize + 2, color);
		graphics.outline(x - 2, y - 2, slotSize + 4, slotSize + 4, color);
	}

	private int withAlpha(int color, float multiplier) {
		int alpha = Math.round((color >>> 24) * Math.clamp(multiplier, 0.0F, 1.0F));
		return color & 0x00FFFFFF | alpha << 24;
	}

	private void drawScaledItem(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, float scale) {
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

	private void drawDurabilityBar(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y,
										  int slotSize, ArmorHudConfig config) {
		if (!stack.isDamageableItem()) {
			return;
		}

		float percent = ArmorHudLayout.durabilityPercent(stack);
		int padding = Math.max(1, Math.round(config.getDurabilityBarHorizontalPadding() * config.getScale()));
		int barWidth = Math.max(1, slotSize - padding * 2);
		int barHeight = Math.max(1, Math.round(config.getDurabilityBarHeight() * config.getScale()));
		int barX = x + padding;
		int barY = y + slotSize - barHeight;
		int fillWidth = Math.round(barWidth * percent);

		graphics.fill(barX, barY, barX + barWidth, barY + barHeight, config.getDurabilityBackgroundColor());
		if (fillWidth > 0) {
			graphics.fill(barX, barY, barX + fillWidth, barY + Math.max(1, barHeight / 2),
				durabilityColor(percent, config, false));
		}
	}

	private void drawDurabilityText(GuiGraphicsExtractor graphics, Minecraft minecraft, ItemStack stack,
										   ArmorHudSlotLayout slot, HudBounds moduleBounds, ArmorHudConfig config) {
		String text = slot.getDurabilityText();
		if (text.isEmpty()) {
			return;
		}

		float renderScale = ArmorHudLayout.durabilityTextRenderScale(config, text);
		float renderedTextWidth = minecraft.font.width(text) * renderScale;
		float renderedTextHeight = minecraft.font.lineHeight * renderScale;
		float textX = moduleBounds.x() + slot.getTextX();
		float textY = moduleBounds.y() + slot.getTextY();

		switch (config.getTextPosition()) {
			case TOP, BOTTOM, CENTER ->
					textX = moduleBounds.x() + slot.getItemX()
							+ (slot.getItemSize() - renderedTextWidth) / 2.0F;
			case LEFT, RIGHT -> {
			}
		}
		switch (config.getTextPosition()) {
			case LEFT, RIGHT, CENTER ->
					textY = moduleBounds.y() + slot.getItemY() + (slot.getItemSize() - renderedTextHeight) / 2.0F;
			case TOP, BOTTOM -> {
			}
		}

		Matrix3x2fStack matrices = graphics.pose();
		matrices.pushMatrix();
		try {
			matrices.translate(textX, textY);
			matrices.scale(renderScale, renderScale);
			int color = config.isColorBasedDurabilityText()
					? durabilityColor(ArmorHudLayout.durabilityPercent(stack), config, true)
					: config.getDurabilityTextColor();
			graphics.text(minecraft.font, text, 0, 0, color, config.isDurabilityTextShadow());
		} finally {
			matrices.popMatrix();
		}
	}

	private int durabilityColor(float percent, ArmorHudConfig config, boolean text) {
		int healthy = text ? config.getTextHealthyColor() : config.getDurabilityHealthyColor();
		int warning = text ? config.getTextWarningColor() : config.getDurabilityWarningColor();
		int critical = text ? config.getTextCriticalColor() : config.getDurabilityCriticalColor();
		if (percent >= 0.5F) {
			return interpolateColor(warning, healthy, (percent - 0.5F) * 2.0F);
		}
		return interpolateColor(critical, warning, percent * 2.0F);
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
