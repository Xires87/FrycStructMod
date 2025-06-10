package net.fryc.frycstructmod.network.s2c;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fryc.frycstructmod.util.interfaces.CanHaveStatusEffect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class AddInactiveStatusEffectS2CPacket {

    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender){
        ClientPlayerEntity player = client.player;
        if(player != null){
            Identifier id = buf.readIdentifier();
            int dur = buf.readInt();
            int amp = buf.readInt();
            boolean icon = buf.readBoolean();

            StatusEffect effect = Registries.STATUS_EFFECT.get(id);
            if(effect != null){
                ((CanHaveStatusEffect) player).addStatusEffectToInactiveEffects(new StatusEffectInstance(effect, dur, amp, false, false, icon));
            }
        }
    }
}
