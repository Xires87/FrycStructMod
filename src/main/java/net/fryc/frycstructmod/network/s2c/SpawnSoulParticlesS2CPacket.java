package net.fryc.frycstructmod.network.s2c;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.ThreadLocalRandom;

public class SpawnSoulParticlesS2CPacket {

    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender){
        ClientWorld world = client.world;
        if(world != null){
            BlockPos pos = buf.readBlockPos();
            for(int i = 0; i < ThreadLocalRandom.current().nextInt(3, 7); i++){
                world.addParticle(
                        ParticleTypes.SOUL,
                        true,
                        pos.getX() + ThreadLocalRandom.current().nextDouble(-0.8D, 0.8D),
                        pos.getY() + ThreadLocalRandom.current().nextDouble(-0.2D, 0.2D),
                        pos.getZ() + ThreadLocalRandom.current().nextDouble(-0.8D, 0.8D),
                        0d,
                        0.1d,
                        0d
                );
            }
        }
    }
}
