package net.fryc.frycstructmod.structure.restrictions;

import net.fryc.frycstructmod.structure.restrictions.sources.RestrictionSource;


public abstract class AbstractStructureRestriction {

    private final String structureId;
    private final RestrictionSource restrictionSource;

    public AbstractStructureRestriction(String structureId, RestrictionSource restrictionSource){
        this.structureId = structureId;
        this.restrictionSource = restrictionSource;
    }

    public String getStructureId() {
        return this.structureId;
    }

    public RestrictionSource getRestrictionSource(){
        return this.restrictionSource;
    }
}
