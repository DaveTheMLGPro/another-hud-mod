package net.davethemlgpro.client.module;

import net.davethemlgpro.client.hud.HudBounds;
import net.davethemlgpro.client.hud.HudSize;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public interface HudModule<C extends HudModuleConfig<C>> {
	Identifier id();
	Component displayName();
	HudSize measure(Minecraft minecraft, C config);
	void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Minecraft minecraft, C config, HudBounds bounds);
}
