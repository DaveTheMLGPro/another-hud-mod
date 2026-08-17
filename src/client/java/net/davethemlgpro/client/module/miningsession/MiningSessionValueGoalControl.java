package net.davethemlgpro.client.module.miningsession;

import net.davethemlgpro.client.screen.popover.HudPopoverControl;
import net.davethemlgpro.client.translation.TranslationKey;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

import java.math.BigDecimal;

final class MiningSessionValueGoalControl implements HudPopoverControl {
	private final MiningSessionHudConfig config;
	private EditBox input;
	private boolean invalidInput;

	MiningSessionValueGoalControl(MiningSessionHudConfig config) {
		this.config = config;
	}

	@Override
	public boolean visible() {
		return config.getGoalMode() == MiningSessionGoalMode.VALUE;
	}

	@Override
	public Component description() {
		return TranslationKey.SETTINGS_MINING_SESSION_VALUE_GOAL_DESCRIPTION.component();
	}

	@Override
	public int height() {
		return 36;
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, Font font, int x, int y, int width,
					   boolean hovered, int accentColor) {
		if (input == null) {
			input = new EditBox(font, x + 3, y + 15, width - 6, 18,
				TranslationKey.SETTINGS_MINING_SESSION_VALUE_GOAL.component());
			input.setMaxLength(32);
			input.setValue(config.getValueGoal().toPlainString());
			input.setResponder(this::updateValue);
		}
		graphics.text(font, TranslationKey.SETTINGS_MINING_SESSION_VALUE_GOAL.component(),
			x + 4, y + 3, invalidInput ? 0xFFFF7777 : 0xFFFFFFFF);
		input.setX(x + 3);
		input.setY(y + 15);
		input.setWidth(width - 6);
		input.extractRenderState(graphics, 0, 0, 0.0F);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
		if (button != 0 || input == null || mouseX < x + 3 || mouseX >= x + width - 3
			|| mouseY < y + 15 || mouseY >= y + 33) {
			return false;
		}
		input.setFocused(true);
		input.setCursorPosition(input.getValue().length());
		return true;
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

	private void updateValue(String value) {
		try {
			BigDecimal parsed = value.isBlank() ? BigDecimal.ZERO : new BigDecimal(value);
			invalidInput = parsed.signum() < 0 || parsed.compareTo(MiningSessionHudConfig.MAX_ITEM_VALUE) > 0;
			if (!invalidInput) {
				config.setValueGoal(parsed);
			}
		} catch (NumberFormatException ignored) {
			invalidInput = true;
		}
	}
}
