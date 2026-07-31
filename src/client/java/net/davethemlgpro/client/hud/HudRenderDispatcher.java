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

import java.util.ArrayList;
import java.util.List;

public class HudRenderDispatcher implements HudElement {
	private final HudModuleRegistry registry;
	private List<HudRenderedElement> lastElements = List.of();

	public HudRenderDispatcher(HudModuleRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		Minecraft minecraft = Minecraft.getInstance();
		int screenWidth = minecraft.getWindow().getGuiScaledWidth();
		int screenHeight = minecraft.getWindow().getGuiScaledHeight();
		HudEditSession editSession = HudEditSession.getActive();
		boolean editorPreview = editSession != null && editSession.isEditorPreview();

		List<HudRenderedElement> renderedElements = new ArrayList<>();

		for (int i = 0; i < registry.getEntries().size(); i++) {
			HudModuleEntry<?> entry = registry.getEntries().get(i);
			HudModuleConfig<?> config = resolveConfig(entry, editSession);
			if (!isModuleEnabled(entry, editSession)) {
				continue;
			}

			int elementCount = entry.elementCountUntyped(config);
			for (int elementIndex = 0; elementIndex < elementCount; elementIndex++) {
				if (!entry.elementVisibleUntyped(config, elementIndex) && !editorPreview) {
					continue;
				}
				HudSize size = entry.measureElementUntyped(minecraft, config, elementIndex, editorPreview);
				if (size.width() == 0 || size.height() == 0) {
					continue;
				}
				HudBounds bounds = HudLayoutEngine.resolve(
					entry.elementLayoutUntyped(config, elementIndex), size, screenWidth, screenHeight);
				renderedElements.add(new HudRenderedElement(i, elementIndex, bounds));
				entry.renderElementUntyped(graphics, deltaTracker, minecraft, config, elementIndex, bounds,
					editorPreview);
			}
		}
		lastElements = List.copyOf(renderedElements);
	}

	public List<HudRenderedElement> getLastElements() {
		return lastElements;
	}

	public HudRenderedElement getLastElement(int moduleIndex, int elementIndex) {
		for (HudRenderedElement element : lastElements) {
			if (element.moduleIndex() == moduleIndex && element.elementIndex() == elementIndex) {
				return element;
			}
		}
		return null;
	}

	private HudModuleConfig<?> resolveConfig(HudModuleEntry<?> entry, HudEditSession editSession) {
		if (editSession == null) {
			return entry.getConfig();
		}
		Identifier id = entry.getModule().id();
		HudConfigSnapshot draft = editSession.getDraft();
		return draft.hasConfig(id) ? draft.getRawConfig(id) : entry.getConfig();
	}

	private boolean isModuleEnabled(HudModuleEntry<?> entry, HudEditSession editSession) {
		if (editSession == null) {
			return entry.isEnabled();
		}
		return editSession.getDraft().isModuleEnabled(entry.getModule().id());
	}
}
