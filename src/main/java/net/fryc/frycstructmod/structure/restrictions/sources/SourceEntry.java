package net.fryc.frycstructmod.structure.restrictions.sources;

import net.fryc.frycstructmod.structure.restrictions.sources.events.Event;
import net.fryc.frycstructmod.util.ServerRestrictionsHelper;
import net.fryc.frycstructmod.util.interfaces.CanBeAffectedByStructure;
import net.fryc.frycstructmod.util.interfaces.PlayerLocator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;

public interface SourceEntry<T> {


    boolean affectOwner(StructureStart structureStart, T source, PlayerEntity player);

    /**
     *  Saves chunk after decreasing source's power and forces StructureRestrictionInstance update on clients
     */
    default boolean affectOwner(ServerWorld world, StructureStart structureStart, T source, PlayerEntity player){
        boolean retValue = this.affectOwner(structureStart, source, player);
        if(retValue){
            ServerRestrictionsHelper.getStructureRestrictionInstance(structureStart).ifPresent(instance -> {
                ServerRestrictionsHelper.sendRestrictionUpdatesToClient(instance, structureStart, world, instance.getStructureId());
            });

            world.getChunk(structureStart.getPos().x, structureStart.getPos().z).setNeedsSaving(true);
        }

        return retValue;
    }

    Event getEvent();


    void setOwner(RestrictionSource owner);

}
