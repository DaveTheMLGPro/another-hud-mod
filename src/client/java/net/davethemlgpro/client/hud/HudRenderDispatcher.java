package net.davethemlgpro.client.hud;

import net.davethemlgpro.client.config.HudConfigSnapshot;
import net.davethemlgpro.client.config.HudEditSession;
import net.davethemlgpro.client.hud.layout.HudLayoutEngine;
import net.davethemlgpro.client.module.HudModuleConfig;
import net.davethemlgpro.client.module.HudModuleEntry;
import net.davethemlgpro.client.module.HudModuleRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public class HudRenderDispatcher implements HudElement {
	private final HudModuleRegistry registry;

	public HudRenderDispatcher(HudModuleRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		Minecraft minecraft = Minecraft.getInstance();
		int screenWidth = minecraft.getWindow().getGuiScaledWidth();
		int screenHeight = minecraft.getWindow().getGuiScaledHeight();
		HudEditSession editSession = HudEditSession.getActive();

		for (HudModuleEntry<?> entry : registry.getEntries()) {
			HudModuleConfig<?> config = resolveConfig(entry, editSession);
			if (!config.enabled()) {
				continue;
			}

			HudSize size = entry.measureUntyped(minecraft, config);
			HudBounds bounds = HudLayoutEngine.resolve(config.getLayout(), size, screenWidth, screenHeight);
			entry.renderUntyped(graphics, deltaTracker, minecraft, config, bounds);
		}
	}

	private HudModuleConfig<?> resolveConfig(HudModuleEntry<?> entry, HudEditSession editSession) {
		if (editSession == null) {
			return entry.getConfig();
		}
		Identifier id = entry.getModule().id();
		HudConfigSnapshot draft = editSession.getDraft();
		return draft.hasConfig(id) ? draft.getRawConfig(id) : entry.getConfig();
	}
}
