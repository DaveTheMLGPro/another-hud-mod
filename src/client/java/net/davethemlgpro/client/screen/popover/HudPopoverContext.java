package net.davethemlgpro.client.screen.popover;

import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public interface HudPopoverContext {
	void openColorPicker(Component title, IntSupplier getter, IntConsumer setter);
}
