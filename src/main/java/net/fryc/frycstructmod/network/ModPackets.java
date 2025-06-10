package net.fryc.frycstructmod.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fryc.frycstructmod.FrycStructMod;
import net.fryc.frycstructmod.network.s2c.*;
import net.minecraft.util.Identifier;

public class ModPackets {

    public static final Identifier AFFECT_BY_STRUCTURE = new Identifier(FrycStructMod.MOD_ID, "affect_by_structure");
    public static final Identifier SPAWN_SOUL_PARTICLES = new Identifier(FrycStructMod.MOD_ID, "spawn_soul_particles");
    public static final Identifier SYNCHRONIZE_STRUCTURE_RESTRICTIONS = new Identifier(FrycStructMod.MOD_ID, "synchronize_structure_restrictions");
    public static final Identifier CLEAR_REGISTERED_STRUCTURE_RESTRICTIONS = new Identifier(FrycStructMod.MOD_ID, "clear_registered_structure_restrictions");
    public static final Identifier ADD_INACTIVE_STATUS_EFFECT = new Identifier(FrycStructMod.MOD_ID, "add_inactive_status_effect");
    public static final Identifier REMOVE_INACTIVE_STATUS_EFFECT = new Identifier(FrycStructMod.MOD_ID, "remove_inactive_status_effect");


    public static void registerS2CPackets(){
        ClientPlayNetworking.registerGlobalReceiver(AFFECT_BY_STRUCTURE, AffectByStructureS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(SPAWN_SOUL_PARTICLES, SpawnSoulParticlesS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(SYNCHRONIZE_STRUCTURE_RESTRICTIONS, SynchronizeStructureRestrictionsS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(CLEAR_REGISTERED_STRUCTURE_RESTRICTIONS, ClearRegisteredStructureRestrictionsS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(ADD_INACTIVE_STATUS_EFFECT, AddInactiveStatusEffectS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(REMOVE_INACTIVE_STATUS_EFFECT, RemoveInactiveStatusEffectS2CPacket::receive);
    }
}
