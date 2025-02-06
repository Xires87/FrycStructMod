package net.fryc.frycstructmod.structure.restrictions.sources;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;

public interface SourceEntry<T> {


    boolean affectOwner(StructureStart structureStart, T source);

    /**
     *  Saves chunk after decreasing source's power
     */
    default boolean affectOwner(ServerWorld world, StructureStart structureStart, T source){
        boolean retValue = this.affectOwner(structureStart, source);
        if(retValue){
            world.getChunk(structureStart.getPos().x, structureStart.getPos().z).setNeedsSaving(true);
        }

        return retValue;
    }

    Class<T> getEntryClass();


    void setOwner(RestrictionSource owner);

}
