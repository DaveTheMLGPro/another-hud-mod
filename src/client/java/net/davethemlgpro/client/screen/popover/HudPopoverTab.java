package net.davethemlgpro.client.screen.popover;

import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Objects;

public record HudPopoverTab(Component title, List<HudPopoverControl> controls) {
	public HudPopoverTab {
		Objects.requireNonNull(title, "Tab title cannot be null.");
		controls = List.copyOf(controls);
	}
}
