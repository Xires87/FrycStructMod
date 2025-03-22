package net.fryc.frycstructmod.structure.restrictions;

import net.fryc.frycstructmod.structure.restrictions.sources.RestrictionSource;


public abstract class AbstractStructureRestriction {

    private final String structureId;
    private final String restrictionType;
    private final RestrictionSource restrictionSource;

    public AbstractStructureRestriction(String structureId, String restrictionType, RestrictionSource restrictionSource){
        this.structureId = structureId;
        this.restrictionType = restrictionType;
        this.restrictionSource = restrictionSource;
    }

    public String getStructureId() {
        return this.structureId;
    }

    public String getRestrictionType(){
        return this.restrictionType;
    }

    public RestrictionSource getRestrictionSource(){
        return this.restrictionSource;
    }
}
