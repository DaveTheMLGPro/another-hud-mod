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

import java.util.Arrays;

public class HudRenderDispatcher implements HudElement {
	private final HudModuleRegistry registry;
	private final HudBounds[] lastBounds;
	private final boolean[] renderedModules;

	public HudRenderDispatcher(HudModuleRegistry registry) {
		this.registry = registry;
		lastBounds = new HudBounds[registry.getEntries().size()];
		renderedModules = new boolean[lastBounds.length];
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		Minecraft minecraft = Minecraft.getInstance();
		int screenWidth = minecraft.getWindow().getGuiScaledWidth();
		int screenHeight = minecraft.getWindow().getGuiScaledHeight();
		HudEditSession editSession = HudEditSession.getActive();
		boolean editorPreview = editSession != null && editSession.isEditorPreview();

		Arrays.fill(renderedModules, false);

		for (int i = 0; i < registry.getEntries().size(); i++) {
			HudModuleEntry<?> entry = registry.getEntries().get(i);
			HudModuleConfig<?> config = resolveConfig(entry, editSession);
			if (!config.enabled() && !editorPreview) {
				continue;
			}

			HudSize size = editorPreview
				? entry.measureEditorPreviewUntyped(minecraft, config)
				: entry.measureUntyped(minecraft, config);
			HudBounds bounds = HudLayoutEngine.resolve(config.getLayout(), size, screenWidth, screenHeight);
			lastBounds[i] = bounds;
			renderedModules[i] = true;
			if (editorPreview) {
				entry.renderEditorPreviewUntyped(graphics, deltaTracker, minecraft, config, bounds);
			} else {
				entry.renderUntyped(graphics, deltaTracker, minecraft, config, bounds);
			}
		}
	}

	public int getTrackedModuleCount() {
		return lastBounds.length;
	}

	public HudBounds getLastBounds(int index) {
		if (index < 0 || index >= lastBounds.length || !renderedModules[index]) {
			return null;
		}
		return lastBounds[index];
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
