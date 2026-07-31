package net.davethemlgpro.client.module.itempickup;

import net.davethemlgpro.client.screen.popover.HudPopoverControl;
import net.davethemlgpro.client.translation.TranslationKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

final class ItemPickupFilterListControl implements HudPopoverControl {
	private static final int INPUT_Y = 14;
	private static final int INPUT_HEIGHT = 18;
	private static final int HELD_Y = 36;
	private static final int HELD_HEIGHT = 18;
	private static final int LIST_Y = 58;
	private static final int ROW_HEIGHT = 18;
	private static final int ADD_WIDTH = 34;

	private final ItemPickupHudConfig config;
	private EditBox input;
	private boolean invalidInput;

	ItemPickupFilterListControl(ItemPickupHudConfig config) {
		this.config = config;
	}

	@Override
	public Component description() {
		return TranslationKey.SETTINGS_ITEM_PICKUP_FILTERED_ITEMS_DESCRIPTION.component();
	}

	@Override
	public int height() {
		return LIST_Y + Math.max(1, config.getFilteredItems().size()) * ROW_HEIGHT + 2;
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, Font font, int x, int y, int width,
					   boolean hovered, int accentColor) {
		ensureInput(font, x, y, width);
		graphics.text(font, TranslationKey.SETTINGS_ITEM_PICKUP_FILTERED_ITEMS.component(),
			x + 4, y + 2, 0xFFFFFFFF);
		input.setX(x + 3);
		input.setY(y + INPUT_Y);
		input.setWidth(width - ADD_WIDTH - 8);
		input.extractRenderState(graphics, 0, 0, 0.0F);
		int addX = x + width - ADD_WIDTH - 3;
		drawButton(graphics, font, TranslationKey.SETTINGS_ITEM_PICKUP_FILTER_ADD.component(),
			addX, y + INPUT_Y, ADD_WIDTH, INPUT_HEIGHT, invalidInput ? 0xFF773333 : 0xFF3A3A3A);
		drawButton(graphics, font, TranslationKey.SETTINGS_ITEM_PICKUP_FILTER_ADD_HELD.component(),
			x + 3, y + HELD_Y, width - 6, HELD_HEIGHT, 0xFF3A3A3A);

		var items = config.getFilteredItems();
		if (items.isEmpty()) {
			graphics.centeredText(font, TranslationKey.SETTINGS_ITEM_PICKUP_FILTER_EMPTY.component(),
				x + width / 2, y + LIST_Y + 5, 0xFF999999);
			return;
		}
		for (int index = 0; index < items.size(); index++) {
			int rowY = y + LIST_Y + index * ROW_HEIGHT;
			graphics.fill(x + 3, rowY, x + width - 3, rowY + ROW_HEIGHT - 2, 0xFF202020);
			graphics.enableScissor(x + 6, rowY, x + width - 22, rowY + ROW_HEIGHT - 2);
			graphics.text(font, Component.literal(items.get(index)), x + 6, rowY + 4, 0xFFDDDDDD);
			graphics.disableScissor();
			graphics.centeredText(font, Component.literal("×"), x + width - 12, rowY + 4, 0xFFFF7777);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
		if (button != 0) {
			return false;
		}
		if (contains(mouseX, mouseY, x + 3, y + INPUT_Y, width - ADD_WIDTH - 8, INPUT_HEIGHT)) {
			input.setFocused(true);
			input.setCursorPosition(input.getValue().length());
			return true;
		}
		input.setFocused(false);
		if (contains(mouseX, mouseY, x + width - ADD_WIDTH - 3, y + INPUT_Y,
			ADD_WIDTH, INPUT_HEIGHT)) {
			submitInput();
			return true;
		}
		if (contains(mouseX, mouseY, x + 3, y + HELD_Y, width - 6, HELD_HEIGHT)) {
			addHeldItem();
			return true;
		}
		var items = config.getFilteredItems();
		for (int index = 0; index < items.size(); index++) {
			if (contains(mouseX, mouseY, x + 3, y + LIST_Y + index * ROW_HEIGHT,
				width - 6, ROW_HEIGHT - 2)) {
				config.removeFilteredItem(items.get(index));
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		return input != null && input.isFocused() && input.keyPressed(event);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		return input != null && input.isFocused() && input.charTyped(event);
	}

	@Override
	public void focusLost() {
		if (input != null) {
			input.setFocused(false);
		}
	}

	private void ensureInput(Font font, int x, int y, int width) {
		if (input != null) {
			return;
		}
		input = new EditBox(font, x + 3, y + INPUT_Y, width - ADD_WIDTH - 8, INPUT_HEIGHT,
			TranslationKey.SETTINGS_ITEM_PICKUP_FILTER_INPUT.component());
		input.setMaxLength(128);
		input.setHint(Component.literal("minecraft:diamond"));
		input.setResponder(value -> invalidInput = false);
	}

	private void submitInput() {
		if (input == null) {
			return;
		}
		String normalized = ItemPickupHudConfig.normalizeItemId(input.getValue());
		if (normalized != null && BuiltInRegistries.ITEM.containsKey(Identifier.parse(normalized))
			&& config.addFilteredItem(normalized)) {
			input.setValue("");
			invalidInput = false;
		} else {
			invalidInput = true;
		}
	}

	private void addHeldItem() {
		Minecraft minecraft = Minecraft.getInstance();
		ItemStack held = minecraft.player == null ? ItemStack.EMPTY : minecraft.player.getMainHandItem();
		if (held.isEmpty()) {
			invalidInput = true;
			return;
		}
		config.addFilteredItem(BuiltInRegistries.ITEM.getKey(held.getItem()).toString());
		invalidInput = false;
	}

	private static void drawButton(GuiGraphicsExtractor graphics, Font font, Component label,
							   int x, int y, int width, int height, int background) {
		graphics.fill(x, y, x + width, y + height, background);
		graphics.outline(x, y, width, height, 0xFF666666);
		graphics.centeredText(font, label, x + width / 2, y + 5, 0xFFFFFFFF);
	}

	private static boolean contains(double mouseX, double mouseY, int x, int y, int width, int height) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}
}
