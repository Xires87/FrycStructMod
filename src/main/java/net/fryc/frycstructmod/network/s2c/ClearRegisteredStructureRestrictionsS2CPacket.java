package net.fryc.frycstructmod.network.s2c;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fryc.frycstructmod.FrycStructMod;
import net.fryc.frycstructmod.structure.restrictions.registry.RestrictionRegistries;
import net.fryc.frycstructmod.util.FrycJsonHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ClearRegisteredStructureRestrictionsS2CPacket {

    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender){
        RestrictionRegistries.STRUCTURE_RESTRICTIONS.clear();
    }
}
