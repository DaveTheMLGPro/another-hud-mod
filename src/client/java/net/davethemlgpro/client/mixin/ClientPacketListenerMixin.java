package net.davethemlgpro.client.mixin;

import net.davethemlgpro.client.AnotherHUDModClient;
import net.davethemlgpro.client.module.itempickup.ItemPickupHudModule;
import net.davethemlgpro.client.module.miningsession.MiningSessionHudModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
	@Inject(method = "handleTakeItemEntity", at = @At("HEAD"))
	private void anotherHudMod$recordItemPickup(ClientboundTakeItemEntityPacket packet, CallbackInfo callbackInfo) {
		Minecraft minecraft = Minecraft.getInstance();
		if (!minecraft.isSameThread() || minecraft.player == null || minecraft.level == null
				|| packet.getPlayerId() != minecraft.player.getId()) {
			return;
		}
		Entity entity = minecraft.level.getEntity(packet.getItemId());
		if (entity instanceof ItemEntity itemEntity) {
			MiningSessionHudModule miningModule = AnotherHUDModClient.getMiningSessionHudModule();
			if (miningModule != null && AnotherHUDModClient.getHudModuleRegistry()
				.isModuleEnabled(MiningSessionHudModule.ID)) {
				miningModule.recordPickup(itemEntity.getItem(), packet.getAmount(),
					AnotherHUDModClient.getMiningSessionHudConfig());
			}
			ItemPickupHudModule module = AnotherHUDModClient.getItemPickupHudModule();
			if (module != null && AnotherHUDModClient.getHudModuleRegistry()
				.isModuleEnabled(ItemPickupHudModule.ID)) {
				module.recordPickup(itemEntity.getItem(), packet.getAmount(), minecraft.level);
			}
		}
	}
}
