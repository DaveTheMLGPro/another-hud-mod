package net.davethemlgpro.client.module.miningsession;

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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.math.BigDecimal;

final class MiningSessionTrackedItemsControl implements HudPopoverControl {
	private static final int INPUT_Y = 14;
	private static final int INPUT_HEIGHT = 18;
	private static final int HELD_Y = 36;
	private static final int HELD_HEIGHT = 18;
	private static final int LIST_Y = 58;
	private static final int ROW_HEIGHT = 18;
	private static final int ADD_WIDTH = 34;
	private static final int VALUE_WIDTH = 58;

	private final MiningSessionHudConfig config;
	private EditBox input;
	private EditBox valueInput;
	private boolean invalidInput;
	private int selectedIndex = -1;
	private int draggingIndex = -1;

	MiningSessionTrackedItemsControl(MiningSessionHudConfig config) {
		this.config = config;
	}

	@Override
	public Component description() {
		return TranslationKey.SETTINGS_MINING_SESSION_TRACKED_ITEMS_DESCRIPTION.component();
	}

	@Override
	public int height() {
		return LIST_Y + Math.max(1, config.getTrackedInventoryItems().size()) * ROW_HEIGHT + 2;
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, Font font, int x, int y, int width,
					   boolean hovered, int accentColor) {
		ensureInput(font, x, y, width);
		graphics.text(font, TranslationKey.SETTINGS_MINING_SESSION_TRACKED_ITEMS.component(),
			x + 4, y + 2, 0xFFFFFFFF);
		input.setX(x + 3);
		input.setY(y + INPUT_Y);
		input.setWidth(width - ADD_WIDTH - VALUE_WIDTH - 10);
		input.extractRenderState(graphics, 0, 0, 0.0F);
		valueInput.setX(x + width - ADD_WIDTH - VALUE_WIDTH - 5);
		valueInput.setY(y + INPUT_Y);
		valueInput.setWidth(VALUE_WIDTH);
		valueInput.extractRenderState(graphics, 0, 0, 0.0F);
		int addX = x + width - ADD_WIDTH - 3;
		drawButton(graphics, font, TranslationKey.SETTINGS_MINING_SESSION_ITEM_ADD.component(),
			addX, y + INPUT_Y, ADD_WIDTH, INPUT_HEIGHT, invalidInput ? 0xFF773333 : 0xFF3A3A3A);
		drawButton(graphics, font, TranslationKey.SETTINGS_MINING_SESSION_ITEM_ADD_HELD.component(),
			x + 3, y + HELD_Y, width - 6, HELD_HEIGHT, 0xFF3A3A3A);

		var items = config.getTrackedInventoryEntries();
		if (items.isEmpty()) {
			graphics.centeredText(font, TranslationKey.SETTINGS_MINING_SESSION_ITEMS_EMPTY.component(),
				x + width / 2, y + LIST_Y + 5, 0xFF999999);
			return;
		}
		long animationTime = System.nanoTime();
		for (int index = 0; index < items.size(); index++) {
			int rowY = y + LIST_Y + index * ROW_HEIGHT;
			graphics.fill(x + 3, rowY, x + width - 3, rowY + ROW_HEIGHT - 2, 0xFF202020);
			if (index == draggingIndex) {
				graphics.outline(x + 3, rowY, width - 6, ROW_HEIGHT - 2, accentColor);
			}
			drawDragHandle(graphics, x + 6, rowY + 5, index == draggingIndex ? accentColor : 0xFF888888);
			MiningSessionTrackedItem item = items.get(index);
			Item registeredItem = BuiltInRegistries.ITEM.getValue(Identifier.parse(item.itemId()));
			ItemStack stack = new ItemStack(registeredItem == null ? Items.BARRIER : registeredItem);
			item.applyAppearance(stack);
			Component name = item.displayName() == null ? stack.getHoverName() : Component.literal(item.displayName());
			String value = MiningSessionHudModule.formatValue(item.unitValue());
			Component label = name.copy().append(" = " + value);
			int color = index == selectedIndex ? accentColor : 0xFFDDDDDD;
			graphics.item(stack, x + 17, rowY);
			int textLeft = x + 36;
			int textRight = x + width - 22;
			int textX = textLeft + MiningSessionMarquee.offset(
				font.width(label), textRight - textLeft, animationTime);
			graphics.enableScissor(textLeft, rowY, textRight, rowY + ROW_HEIGHT - 2);
			graphics.text(font, label, textX, rowY + 4, color);
			graphics.disableScissor();
			graphics.centeredText(font, Component.literal("×"), x + width - 12, rowY + 4, 0xFFFF7777);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
		if (button != 0) {
			return false;
		}
		draggingIndex = -1;
		if (contains(mouseX, mouseY, x + 3, y + INPUT_Y,
			width - ADD_WIDTH - VALUE_WIDTH - 10, INPUT_HEIGHT)) {
			input.setFocused(true);
			valueInput.setFocused(false);
			input.setCursorPosition(input.getValue().length());
			return true;
		}
		if (contains(mouseX, mouseY, x + width - ADD_WIDTH - VALUE_WIDTH - 5, y + INPUT_Y,
			VALUE_WIDTH, INPUT_HEIGHT)) {
			input.setFocused(false);
			valueInput.setFocused(true);
			valueInput.setCursorPosition(valueInput.getValue().length());
			return true;
		}
		input.setFocused(false);
		valueInput.setFocused(false);
		if (contains(mouseX, mouseY, x + width - ADD_WIDTH - 3, y + INPUT_Y, ADD_WIDTH, INPUT_HEIGHT)) {
			submitInput();
			return true;
		}
		if (contains(mouseX, mouseY, x + 3, y + HELD_Y, width - 6, HELD_HEIGHT)) {
			addHeldItem();
			return true;
		}
		var items = config.getTrackedInventoryEntries();
		for (int index = 0; index < items.size(); index++) {
			if (contains(mouseX, mouseY, x + 3, y + LIST_Y + index * ROW_HEIGHT,
				width - 6, ROW_HEIGHT - 2)) {
				MiningSessionTrackedItem item = items.get(index);
				if (mouseX >= x + width - 22) {
					config.removeTrackedInventoryItem(index);
					selectedIndex = -1;
				} else {
					selectedIndex = index;
					draggingIndex = index;
					input.setValue(item.itemId());
					valueInput.setValue(item.unitValue().toPlainString());
					input.setFocused(true);
					input.setCursorPosition(input.getValue().length());
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int x, int y, int width) {
		if (draggingIndex < 0) {
			return false;
		}
		int itemCount = config.getTrackedInventoryEntries().size();
		if (itemCount == 0) {
			draggingIndex = -1;
			return false;
		}
		int targetIndex = Math.clamp((int) Math.floor((mouseY - y - LIST_Y) / ROW_HEIGHT),
			0, itemCount - 1);
		if (config.moveTrackedInventoryItem(draggingIndex, targetIndex)) {
			draggingIndex = targetIndex;
			selectedIndex = targetIndex;
		}
		return true;
	}

	@Override
	public void mouseReleased() {
		draggingIndex = -1;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		return input != null && (input.isFocused() && input.keyPressed(event)
			|| valueInput.isFocused() && valueInput.keyPressed(event));
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		return input != null && (input.isFocused() && input.charTyped(event)
			|| valueInput.isFocused() && valueInput.charTyped(event));
	}

	@Override
	public void focusLost() {
		if (input != null) {
			input.setFocused(false);
			valueInput.setFocused(false);
		}
	}

	private void ensureInput(Font font, int x, int y, int width) {
		if (input != null) {
			return;
		}
		input = new EditBox(font, x + 3, y + INPUT_Y,
			width - ADD_WIDTH - VALUE_WIDTH - 10, INPUT_HEIGHT,
			TranslationKey.SETTINGS_MINING_SESSION_ITEM_INPUT.component());
		input.setMaxLength(128);
		input.setHint(Component.literal("Diamond"));
		input.setResponder(value -> invalidInput = false);
		valueInput = new EditBox(font, x + width - ADD_WIDTH - VALUE_WIDTH - 5, y + INPUT_Y,
			VALUE_WIDTH, INPUT_HEIGHT, TranslationKey.SETTINGS_MINING_SESSION_ITEM_VALUE.component());
		valueInput.setMaxLength(32);
		valueInput.setHint(TranslationKey.SETTINGS_MINING_SESSION_ITEM_VALUE.component());
		valueInput.setResponder(value -> invalidInput = false);
	}

	private void submitInput() {
		if (input == null) {
			return;
		}
		String normalized = MiningSessionHudConfig.normalizeItemId(input.getValue());
		BigDecimal value = parseValue();
		var entries = config.getTrackedInventoryEntries();
		boolean updatedSelected = selectedIndex >= 0 && selectedIndex < entries.size()
			&& entries.get(selectedIndex).itemId().equals(normalized) && value != null;
		boolean saved = updatedSelected || normalized != null && value != null
			&& BuiltInRegistries.ITEM.containsKey(Identifier.parse(normalized))
			&& config.putTrackedInventoryItem(normalized, value);
		if (saved) {
			if (updatedSelected) {
				config.setTrackedInventoryItemValue(selectedIndex, value);
			}
			input.setValue("");
			valueInput.setValue("");
			selectedIndex = -1;
			invalidInput = false;
		} else {
			invalidInput = true;
		}
	}

	private void addHeldItem() {
		Minecraft minecraft = Minecraft.getInstance();
		ItemStack held = minecraft.player == null ? ItemStack.EMPTY : minecraft.player.getMainHandItem();
		BigDecimal value = parseValue();
		if (held.isEmpty() || value == null || !config.putHeldTrackedInventoryItem(held, value)) {
			invalidInput = true;
			return;
		}
		selectedIndex = -1;
		invalidInput = false;
	}

	private BigDecimal parseValue() {
		if (valueInput == null || valueInput.getValue().isBlank()) {
			return BigDecimal.ZERO;
		}
		try {
			BigDecimal parsed = new BigDecimal(valueInput.getValue());
			return parsed.signum() < 0 || parsed.compareTo(MiningSessionHudConfig.MAX_ITEM_VALUE) > 0
				? null : parsed;
		} catch (NumberFormatException ignored) {
			return null;
		}
	}

	private static void drawButton(GuiGraphicsExtractor graphics, Font font, Component label,
							   int x, int y, int width, int height, int background) {
		graphics.fill(x, y, x + width, y + height, background);
		graphics.outline(x, y, width, height, 0xFF666666);
		graphics.centeredText(font, label, x + width / 2, y + 5, 0xFFFFFFFF);
	}

	private static void drawDragHandle(GuiGraphicsExtractor graphics, int x, int y, int color) {
		for (int offset = 0; offset < 3; offset++) {
			graphics.fill(x, y + offset * 3, x + 7, y + offset * 3 + 1, color);
		}
	}

	private static boolean contains(double mouseX, double mouseY, int x, int y, int width, int height) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}
}
