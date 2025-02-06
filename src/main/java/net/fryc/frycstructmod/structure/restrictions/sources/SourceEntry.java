package net.fryc.frycstructmod.structure.restrictions.sources;

import net.minecraft.structure.StructureStart;

public interface SourceEntry<T> {


    boolean affectOwner(StructureStart structureStart, T source);

    Class<T> getEntryClass();


    void setOwner(RestrictionSource owner);

}
