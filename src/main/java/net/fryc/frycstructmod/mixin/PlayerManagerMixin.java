package net.fryc.frycstructmod.mixin;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fryc.frycstructmod.network.ModPackets;
import net.fryc.frycstructmod.structure.restrictions.json.StructureRestrictionsResourceReloadListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Consumer;

@Mixin(PlayerManager.class)
abstract class PlayerManagerMixin {

    @Shadow
    private @Final List<ServerPlayerEntity> players;

    @Inject(method = "onPlayerConnect(Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;)V", at = @At("HEAD"))
    private void synchronizeStructureRestrictionsOnConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo info){
        connection.send(ServerPlayNetworking.createS2CPacket(ModPackets.CLEAR_REGISTERED_STRUCTURE_RESTRICTIONS, PacketByteBufs.empty()));
        sendJsonsToClient(buf -> {
            connection.send(ServerPlayNetworking.createS2CPacket(ModPackets.SYNCHRONIZE_STRUCTURE_RESTRICTIONS, buf));
        });
    }

    @Inject(method = "onDataPacksReloaded()V", at = @At("TAIL"))
    private void synchronizeStructureRestrictionsOnReload(CallbackInfo info) {
        for(ServerPlayerEntity serverPlayerEntity : this.players) {
            ServerPlayNetworking.send(serverPlayerEntity, ModPackets.CLEAR_REGISTERED_STRUCTURE_RESTRICTIONS, PacketByteBufs.empty());
            sendJsonsToClient(buf -> {
                ServerPlayNetworking.send(serverPlayerEntity, ModPackets.SYNCHRONIZE_STRUCTURE_RESTRICTIONS, buf);
            });
        }

    }

    private static void sendJsonsToClient(Consumer<PacketByteBuf> sendAction){
        //sending multiple smaller packets instead of one large packet
        StructureRestrictionsResourceReloadListener.SAVED_JSONS.forEach((id, jsonString) -> {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeIdentifier(id);
            buf.writeString(jsonString);
            sendAction.accept(buf);
        });
    }
}
