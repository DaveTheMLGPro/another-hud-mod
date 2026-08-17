package net.davethemlgpro.client.mixin;

import net.davethemlgpro.client.module.containersearch.ContainerSearchOverlay;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenOverlayMixin {
	@Inject(
		method = "extractRenderState",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;extractTooltip(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V",
			shift = At.Shift.BEFORE
		)
	)
	private void anotherHudMod$renderContainerSearchBeforeTooltip(GuiGraphicsExtractor graphics,
															 int mouseX, int mouseY, float tickProgress,
															 CallbackInfo callbackInfo) {
		ContainerSearchOverlay.renderBeforeTooltip((Screen) (Object) this, graphics, mouseX, mouseY, tickProgress);
	}
}
