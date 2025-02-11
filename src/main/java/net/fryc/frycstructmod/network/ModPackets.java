package net.fryc.frycstructmod.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fryc.frycstructmod.FrycStructMod;
import net.fryc.frycstructmod.network.s2c.AffectByStructureS2CPacket;
import net.fryc.frycstructmod.network.s2c.SpawnSoulParticlesS2CPacket;
import net.minecraft.util.Identifier;

public class ModPackets {

    public static final Identifier AFFECT_BY_STRUCTURE = new Identifier(FrycStructMod.MOD_ID, "affect_by_structure");
    public static final Identifier SPAWN_SOUL_PARTICLES = new Identifier(FrycStructMod.MOD_ID, "spawn_soul_particles");


    public static void registerS2CPackets(){
        ClientPlayNetworking.registerGlobalReceiver(AFFECT_BY_STRUCTURE, AffectByStructureS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(SPAWN_SOUL_PARTICLES, SpawnSoulParticlesS2CPacket::receive);
    }
}
