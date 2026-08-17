package net.davethemlgpro.client.module.containersearch;

import net.davethemlgpro.AnotherHUDMod;
import net.davethemlgpro.client.AnotherHUDModClient;
import net.davethemlgpro.client.config.EditorConfig;
import net.davethemlgpro.client.config.HudEditSession;
import net.davethemlgpro.client.hud.HudBounds;
import net.davethemlgpro.client.hud.HudSize;
import net.davethemlgpro.client.module.HudModule;
import net.davethemlgpro.client.translation.TranslationKey;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class ContainerSearchHudModule implements HudModule<ContainerSearchHudConfig> {
	public static final Identifier ID = AnotherHUDMod.id("container_search");
	public static final int PANEL_WIDTH = 172;
	public static final int PANEL_HEIGHT = 124;

	@Override
	public Identifier id() {
		return ID;
	}

	@Override
	public Component displayName() {
		return TranslationKey.MODULE_CONTAINER_SEARCH.component();
	}

	@Override
	public Component description() {
		return TranslationKey.MODULE_CONTAINER_SEARCH_DESCRIPTION.component();
	}

	@Override
	public HudSize measure(Minecraft minecraft, ContainerSearchHudConfig config) {
		return new HudSize(0, 0);
	}

	@Override
	public HudSize measureEditorPreview(Minecraft minecraft, ContainerSearchHudConfig config) {
		return panelSize(config);
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Minecraft minecraft,
					   ContainerSearchHudConfig config, HudBounds bounds) {
	}

	@Override
	public void renderEditorPreview(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Minecraft minecraft,
								ContainerSearchHudConfig config, HudBounds bounds) {
		float scale = (float) config.getUiScale();
		graphics.pose().pushMatrix();
		graphics.pose().translate(bounds.x(), bounds.y());
		graphics.pose().scale(scale, scale);
		ContainerSearchPanelRenderer.renderPanel(graphics, minecraft, config,
			new HudBounds(0, 0, PANEL_WIDTH, PANEL_HEIGHT),
			TranslationKey.CONTAINER_SEARCH_PREVIEW_QUERY.component(),
			TranslationKey.CONTAINER_SEARCH_STATUS_MANY.component(3, 147), false,
			sharedAccentColor(), false, false);
		graphics.pose().popMatrix();
	}

	public static HudSize panelSize(ContainerSearchHudConfig config) {
		return new HudSize(
			Math.max(1, (int) Math.round(PANEL_WIDTH * config.getUiScale())),
			Math.max(1, (int) Math.round(PANEL_HEIGHT * config.getUiScale())));
	}

	static int sharedAccentColor() {
		HudEditSession session = HudEditSession.getActive();
		if (session != null) {
			return session.getDraft().getRawEditor().getAccentColor();
		}
		return AnotherHUDModClient.getHudConfigManager() == null
			? EditorConfig.DEFAULT_ACCENT_COLOR
			: AnotherHUDModClient.getHudConfigManager().getEditorConfig().getAccentColor();
	}
}
