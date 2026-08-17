package net.davethemlgpro.client.module.miningsession;

import net.davethemlgpro.client.screen.popover.HudPopoverControl;
import net.davethemlgpro.client.translation.TranslationKey;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

final class MiningSessionItemGoalsControl implements HudPopoverControl {
	private static final int INPUT_Y = 14;
	private static final int INPUT_HEIGHT = 18;
	private static final int HELD_Y = 36;
	private static final int NOTICE_Y = 58;
	private static final int LIST_Y = 72;
	private static final int ROW_HEIGHT = 18;
	private static final int MAX_VISIBLE_ROWS = 5;
	private static final int ADD_WIDTH = 34;
	private static final int AMOUNT_WIDTH = 58;

	private final MiningSessionHudConfig config;
	private EditBox itemInput;
	private EditBox amountInput;
	private boolean invalidInput;
	private boolean limitReached;
	private int listScroll;
	private boolean draggingScrollbar;

	MiningSessionItemGoalsControl(MiningSessionHudConfig config) {
		this.config = config;
	}

	@Override
	public boolean visible() {
		return config.getGoalMode() == MiningSessionGoalMode.ITEMS;
	}

	@Override
	public Component description() {
		return TranslationKey.SETTINGS_MINING_SESSION_ITEM_GOALS_DESCRIPTION.component();
	}

	@Override
	public int height() {
		return LIST_Y + Math.max(1, Math.min(MAX_VISIBLE_ROWS, config.getItemGoals().size())) * ROW_HEIGHT + 2;
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, Font font, int x, int y, int width,
					   boolean hovered, int accentColor) {
		ensureInputs(font, x, y, width);
		graphics.text(font, TranslationKey.SETTINGS_MINING_SESSION_ITEM_GOALS.component(),
			x + 4, y + 2, 0xFFFFFFFF);
		itemInput.setX(x + 3);
		itemInput.setY(y + INPUT_Y);
		itemInput.setWidth(width - ADD_WIDTH - AMOUNT_WIDTH - 10);
		itemInput.extractRenderState(graphics, 0, 0, 0.0F);
		amountInput.setX(x + width - ADD_WIDTH - AMOUNT_WIDTH - 5);
		amountInput.setY(y + INPUT_Y);
		amountInput.setWidth(AMOUNT_WIDTH);
		amountInput.extractRenderState(graphics, 0, 0, 0.0F);
		int addX = x + width - ADD_WIDTH - 3;
		drawButton(graphics, font, TranslationKey.SETTINGS_MINING_SESSION_ITEM_ADD.component(),
			addX, y + INPUT_Y, ADD_WIDTH, INPUT_HEIGHT, invalidInput ? 0xFF773333 : 0xFF3A3A3A);
		drawButton(graphics, font, TranslationKey.SETTINGS_MINING_SESSION_ITEM_ADD_HELD.component(),
			x + 3, y + HELD_Y, width - 6, INPUT_HEIGHT, 0xFF3A3A3A);

		var goals = config.getItemGoals();
		if (limitReached || goals.size() >= MiningSessionHudConfig.MAX_ITEM_GOALS) {
			graphics.centeredText(font, TranslationKey.SETTINGS_MINING_SESSION_GOAL_LIMIT_REACHED.component(
				MiningSessionHudConfig.MAX_ITEM_GOALS), x + width / 2, y + NOTICE_Y + 2, 0xFFFF7777);
		}
		clampListScroll(goals.size());
		if (goals.isEmpty()) {
			graphics.centeredText(font, TranslationKey.SETTINGS_MINING_SESSION_GOALS_EMPTY.component(),
				x + width / 2, y + LIST_Y + 5, 0xFF999999);
			return;
		}
		int visibleRows = Math.min(MAX_VISIBLE_ROWS, goals.size());
		boolean scrollable = goals.size() > MAX_VISIBLE_ROWS;
		long animationTime = System.nanoTime();
		for (int visibleIndex = 0; visibleIndex < visibleRows; visibleIndex++) {
			int index = listScroll + visibleIndex;
			MiningSessionItemGoal goal = goals.get(index);
			int rowY = y + LIST_Y + visibleIndex * ROW_HEIGHT;
			int rowRight = x + width - (scrollable ? 9 : 3);
			graphics.fill(x + 3, rowY, rowRight, rowY + ROW_HEIGHT - 2, 0xFF202020);
			Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(goal.itemId()));
			ItemStack stack = new ItemStack(item == null ? Items.BARRIER : item);
			graphics.item(stack, x + 5, rowY);
			Component label = stack.getHoverName().copy().append(" × " + goal.targetAmount());
			int textLeft = x + 24;
			int textRight = rowRight - 20;
			int textX = textLeft + MiningSessionMarquee.offset(
				font.width(label), textRight - textLeft, animationTime);
			graphics.enableScissor(textLeft, rowY, textRight, rowY + ROW_HEIGHT - 2);
			graphics.text(font, label, textX, rowY + 4, 0xFFDDDDDD);
			graphics.disableScissor();
			graphics.centeredText(font, Component.literal("×"), rowRight - 10, rowY + 4, 0xFFFF7777);
		}
		if (scrollable) {
			renderScrollbar(graphics, x + width - 6, y + LIST_Y,
				visibleRows * ROW_HEIGHT - 2, goals.size());
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
		if (button != 0 || itemInput == null) {
			return false;
		}
		if (contains(mouseX, mouseY, x + 3, y + INPUT_Y,
			width - ADD_WIDTH - AMOUNT_WIDTH - 10, INPUT_HEIGHT)) {
			focus(itemInput);
			return true;
		}
		if (contains(mouseX, mouseY, x + width - ADD_WIDTH - AMOUNT_WIDTH - 5, y + INPUT_Y,
			AMOUNT_WIDTH, INPUT_HEIGHT)) {
			focus(amountInput);
			return true;
		}
		itemInput.setFocused(false);
		amountInput.setFocused(false);
		if (contains(mouseX, mouseY, x + width - ADD_WIDTH - 3, y + INPUT_Y, ADD_WIDTH, INPUT_HEIGHT)) {
			submit();
			return true;
		}
		if (contains(mouseX, mouseY, x + 3, y + HELD_Y, width - 6, INPUT_HEIGHT)) {
			addHeldItem();
			return true;
		}
		var goals = config.getItemGoals();
		if (goals.size() > MAX_VISIBLE_ROWS && contains(mouseX, mouseY,
			x + width - 9, y + LIST_Y, 9, MAX_VISIBLE_ROWS * ROW_HEIGHT)) {
			draggingScrollbar = true;
			setListScrollFromMouse(mouseY, y + LIST_Y, goals.size());
			return true;
		}
		int visibleRows = Math.min(MAX_VISIBLE_ROWS, goals.size());
		for (int visibleIndex = 0; visibleIndex < visibleRows; visibleIndex++) {
			int index = listScroll + visibleIndex;
			if (contains(mouseX, mouseY, x + 3, y + LIST_Y + visibleIndex * ROW_HEIGHT,
				width - 6, ROW_HEIGHT - 2)) {
				int deleteX = x + width - (goals.size() > MAX_VISIBLE_ROWS ? 29 : 22);
				if (mouseX >= deleteX) {
					config.removeItemGoal(index);
					limitReached = false;
					clampListScroll(config.getItemGoals().size());
				} else {
					MiningSessionItemGoal goal = goals.get(index);
					itemInput.setValue(goal.itemId());
					amountInput.setValue(Integer.toString(goal.targetAmount()));
					focus(itemInput);
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int x, int y, int width) {
		if (!draggingScrollbar) {
			return false;
		}
		setListScrollFromMouse(mouseY, y + LIST_Y, config.getItemGoals().size());
		return true;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollY, int x, int y, int width) {
		int itemCount = config.getItemGoals().size();
		if (itemCount <= MAX_VISIBLE_ROWS || !contains(mouseX, mouseY,
			x + 3, y + LIST_Y, width - 6, MAX_VISIBLE_ROWS * ROW_HEIGHT)) {
			return false;
		}
		listScroll = Math.clamp(listScroll - (int) Math.round(scrollY),
			0, itemCount - MAX_VISIBLE_ROWS);
		return true;
	}

	@Override
	public void mouseReleased() {
		draggingScrollbar = false;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		return itemInput != null && (itemInput.isFocused() && itemInput.keyPressed(event)
			|| amountInput.isFocused() && amountInput.keyPressed(event));
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		return itemInput != null && (itemInput.isFocused() && itemInput.charTyped(event)
			|| amountInput.isFocused() && amountInput.charTyped(event));
	}

	@Override
	public void focusLost() {
		if (itemInput != null) {
			itemInput.setFocused(false);
			amountInput.setFocused(false);
		}
	}

	private void ensureInputs(Font font, int x, int y, int width) {
		if (itemInput != null) {
			return;
		}
		itemInput = new EditBox(font, x + 3, y + INPUT_Y,
			width - ADD_WIDTH - AMOUNT_WIDTH - 10, INPUT_HEIGHT,
			TranslationKey.SETTINGS_MINING_SESSION_ITEM_INPUT.component());
		itemInput.setMaxLength(128);
		itemInput.setHint(Component.literal("Diamond"));
		itemInput.setResponder(value -> {
			invalidInput = false;
			limitReached = false;
		});
		amountInput = new EditBox(font, x + width - ADD_WIDTH - AMOUNT_WIDTH - 5, y + INPUT_Y,
			AMOUNT_WIDTH, INPUT_HEIGHT, TranslationKey.SETTINGS_MINING_SESSION_GOAL_AMOUNT.component());
		amountInput.setMaxLength(10);
		amountInput.setHint(TranslationKey.SETTINGS_MINING_SESSION_GOAL_AMOUNT.component());
		amountInput.setResponder(value -> {
			invalidInput = false;
			limitReached = false;
		});
	}

	private void submit() {
		String normalized = MiningSessionHudConfig.normalizeItemId(itemInput.getValue());
		Integer amount = parseAmount(true);
		boolean saved = normalized != null && amount != null
			&& BuiltInRegistries.ITEM.containsKey(Identifier.parse(normalized))
			&& config.putItemGoal(normalized, amount);
		limitReached = normalized != null && !config.getItemGoalIds().contains(normalized)
			&& config.getItemGoals().size() >= MiningSessionHudConfig.MAX_ITEM_GOALS;
		invalidInput = !saved;
		if (saved) {
			itemInput.setValue("");
			amountInput.setValue("");
			listScroll = Math.max(0, config.getItemGoals().size() - MAX_VISIBLE_ROWS);
			limitReached = config.getItemGoals().size() >= MiningSessionHudConfig.MAX_ITEM_GOALS;
		}
	}

	private void addHeldItem() {
		ItemStack held = Minecraft.getInstance().player == null
			? ItemStack.EMPTY : Minecraft.getInstance().player.getMainHandItem();
		Integer amount = parseAmount(true);
		if (held.isEmpty() || amount == null) {
			invalidInput = true;
			return;
		}
		String itemId = BuiltInRegistries.ITEM.getKey(held.getItem()).toString();
		boolean saved = config.putItemGoal(itemId, amount);
		limitReached = !config.getItemGoalIds().contains(itemId)
			&& config.getItemGoals().size() >= MiningSessionHudConfig.MAX_ITEM_GOALS;
		invalidInput = !saved;
		if (saved) {
			itemInput.setValue("");
			amountInput.setValue("");
			listScroll = Math.max(0, config.getItemGoals().size() - MAX_VISIBLE_ROWS);
			limitReached = config.getItemGoals().size() >= MiningSessionHudConfig.MAX_ITEM_GOALS;
		}
	}

	private Integer parseAmount(boolean defaultToOne) {
		if (amountInput.getValue().isBlank() && defaultToOne) {
			return 1;
		}
		try {
			int amount = Integer.parseInt(amountInput.getValue());
			return amount > 0 && amount <= MiningSessionHudConfig.MAX_GOAL_ITEM_AMOUNT ? amount : null;
		} catch (NumberFormatException ignored) {
			return null;
		}
	}

	private void clampListScroll(int itemCount) {
		listScroll = Math.clamp(listScroll, 0, Math.max(0, itemCount - MAX_VISIBLE_ROWS));
	}

	private void renderScrollbar(GuiGraphicsExtractor graphics, int x, int y, int height, int itemCount) {
		int thumbHeight = Math.max(12, height * MAX_VISIBLE_ROWS / itemCount);
		int travel = height - thumbHeight;
		int maximumScroll = itemCount - MAX_VISIBLE_ROWS;
		int thumbY = y + (maximumScroll == 0 ? 0 : listScroll * travel / maximumScroll);
		graphics.fill(x, y, x + 3, y + height, 0xFF151515);
		graphics.fill(x, thumbY, x + 3, thumbY + thumbHeight, 0xFFAAAAAA);
	}

	private void setListScrollFromMouse(double mouseY, int trackY, int itemCount) {
		int trackHeight = MAX_VISIBLE_ROWS * ROW_HEIGHT - 2;
		int thumbHeight = Math.max(12, trackHeight * MAX_VISIBLE_ROWS / itemCount);
		int travel = trackHeight - thumbHeight;
		if (travel <= 0) {
			listScroll = 0;
			return;
		}
		int thumbTop = Math.clamp((int) Math.round(mouseY) - thumbHeight / 2,
			trackY, trackY + trackHeight - thumbHeight);
		listScroll = (thumbTop - trackY) * (itemCount - MAX_VISIBLE_ROWS) / travel;
	}

	private void focus(EditBox input) {
		itemInput.setFocused(input == itemInput);
		amountInput.setFocused(input == amountInput);
		input.setCursorPosition(input.getValue().length());
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
