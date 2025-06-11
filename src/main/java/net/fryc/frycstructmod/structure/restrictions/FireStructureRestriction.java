package net.fryc.frycstructmod.structure.restrictions;

import net.fryc.frycstructmod.structure.restrictions.registry.RestrictionTypes;
import net.fryc.frycstructmod.structure.restrictions.sources.RestrictionSource;

import java.util.Random;

public class FireStructureRestriction extends AbstractStructureRestriction {

    private final boolean doFireTick;
    private final float nonTickingFireRemoveChance;

    public FireStructureRestriction(String structureId, boolean doFireTick, float nonTickingFireRemoveChance, RestrictionSource restrictionSource) {
        super(structureId, RestrictionTypes.FIRE, restrictionSource);
        this.doFireTick =  doFireTick;
        this.nonTickingFireRemoveChance = nonTickingFireRemoveChance;
    }

    public boolean shouldRemoveFire(Random random){
        return random.nextFloat() <= this.nonTickingFireRemoveChance;
    }

    public boolean shouldTickFire(){
        return this.doFireTick;
    }

    public float getNonTickingFireRemoveChance(){
        return this.nonTickingFireRemoveChance;
    }
}
