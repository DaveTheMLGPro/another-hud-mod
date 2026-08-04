package net.davethemlgpro.client.module;

import net.davethemlgpro.client.hud.HudBounds;
import net.davethemlgpro.client.hud.HudSize;
import net.davethemlgpro.client.hud.layout.ModuleLayout;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public interface HudModule<C extends HudModuleConfig<C>> {
	Identifier id();
	Component displayName();

	default Component description() {
		return Component.empty();
	}

	HudSize measure(Minecraft minecraft, C config);
	void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Minecraft minecraft, C config, HudBounds bounds);

	default int elementCount(C config) {
		return 1;
	}

	default ModuleLayout elementLayout(C config, int elementIndex) {
		requireDefaultElement(elementIndex);
		return config.getLayout();
	}

	default boolean elementVisible(C config, int elementIndex) {
		requireDefaultElement(elementIndex);
		return config.visible();
	}

	default void setElementVisible(C config, int elementIndex, boolean visible) {
		requireDefaultElement(elementIndex);
		config.setVisible(visible);
	}

	default HudSize measureElement(Minecraft minecraft, C config, int elementIndex, boolean editorPreview) {
		requireDefaultElement(elementIndex);
		return editorPreview ? measureEditorPreview(minecraft, config) : measure(minecraft, config);
	}

	default void renderElement(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Minecraft minecraft,
							   C config, int elementIndex, HudBounds bounds, boolean editorPreview) {
		requireDefaultElement(elementIndex);
		if (editorPreview) {
			renderEditorPreview(graphics, deltaTracker, minecraft, config, bounds);
		} else {
			render(graphics, deltaTracker, minecraft, config, bounds);
		}
	}

	default HudSize measureEditorPreview(Minecraft minecraft, C config) {
		return measure(minecraft, config);
	}

	default void renderEditorPreview(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Minecraft minecraft,
									 C config, HudBounds bounds) {
		render(graphics, deltaTracker, minecraft, config, bounds);
	}

	private static void requireDefaultElement(int elementIndex) {
		if (elementIndex != 0) {
			throw new IndexOutOfBoundsException("Single-element HUD module has no element " + elementIndex);
		}
	}
}
