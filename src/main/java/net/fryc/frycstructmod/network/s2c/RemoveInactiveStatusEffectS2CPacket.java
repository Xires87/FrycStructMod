package net.fryc.frycstructmod.network.s2c;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fryc.frycstructmod.util.interfaces.CanHaveStatusEffect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class RemoveInactiveStatusEffectS2CPacket {

    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender){
        ClientPlayerEntity player = client.player;
        if(player != null){
            Identifier id = buf.readIdentifier();

            ((CanHaveStatusEffect) player).getInactiveStatusEffects().entrySet().removeIf(entry -> {
                return id.equals(Registries.STATUS_EFFECT.getId(entry.getKey()));
            });
        }
    }
}
