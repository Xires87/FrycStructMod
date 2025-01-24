package net.fryc.frycstructmod.network.s2c;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fryc.frycstructmod.util.CanBeAffectedByStructure;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;

public class AffectByStructureS2CPacket {

    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender){
        ClientPlayerEntity player = client.player;
        if(player != null){
            ((CanBeAffectedByStructure) player).setAffectedByStructure(buf.readBoolean());
        }
    }
}
