package net.fryc.frycstructmod.network.s2c;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fryc.frycstructmod.structure.restrictions.AbstractStructureRestriction;
import net.fryc.frycstructmod.structure.restrictions.StructureRestrictionInstance;
import net.fryc.frycstructmod.structure.restrictions.registry.RestrictionRegistries;
import net.fryc.frycstructmod.util.interfaces.CanBeAffectedByStructure;
import net.fryc.frycstructmod.util.interfaces.client.HoldsStructureRestrictionInstance;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;

import java.util.HashMap;

public class AffectByStructureS2CPacket {

    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender){
        ClientPlayerEntity player = client.player;
        if(player != null){
            String id = buf.readString();
            ((CanBeAffectedByStructure) player).setAffectedByStructure(id);
            if(!id.isEmpty() && buf.isReadable()){
                int sharedPower = buf.readInt();
                HashMap<String, AbstractStructureRestriction> restrictions = RestrictionRegistries.STRUCTURE_RESTRICTIONS.get(id);
                StructureRestrictionInstance instance = new StructureRestrictionInstance(restrictions.values());

                instance.setCurrentSharedPower(sharedPower);
                while(buf.isReadable()){
                    String resId = buf.readString();
                    int sepPower = buf.readInt();
                    instance.getCurrentSeperatePowers().put(restrictions.get(resId), sepPower);
                }

                ((HoldsStructureRestrictionInstance) player).setStructureRestrictionInstance(instance);
                instance.updateDisabledRestrictions();
            }
            else {
                ((HoldsStructureRestrictionInstance) player).setStructureRestrictionInstance(null);
            }
        }
    }
}
