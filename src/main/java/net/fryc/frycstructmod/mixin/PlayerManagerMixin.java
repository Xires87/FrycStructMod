package net.fryc.frycstructmod.mixin;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fryc.frycstructmod.network.ModPackets;
import net.fryc.frycstructmod.structure.restrictions.json.StructureRestrictionsResourceReloadListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
abstract class PlayerManagerMixin {

    @Inject(method = "onPlayerConnect(Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;)V", at = @At("HEAD"))
    private void synchronizeStructureRestrictions(ClientConnection connection, ServerPlayerEntity player, CallbackInfo info){
        //sending multiple smaller packets instead of one large packet
        StructureRestrictionsResourceReloadListener.SAVED_JSONS.forEach((id, jsonString) -> {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeIdentifier(id);
            buf.writeString(jsonString);
            connection.send(ServerPlayNetworking.createS2CPacket(ModPackets.SYNCHRONIZE_STRUCTURE_RESTRICTIONS, buf));
        });
    }
}
