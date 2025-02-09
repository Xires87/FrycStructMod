package net.fryc.frycstructmod.structure.restrictions.sources;

import net.fryc.frycstructmod.util.RestrictionsHelper;
import net.fryc.frycstructmod.util.interfaces.HasRestrictions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;

public class PersistentMobSourceEntry extends LivingEntitySourceEntry {

    public PersistentMobSourceEntry(Identifier sourceId, int sourceStrength) {
        super(sourceId, sourceStrength);
    }

    @Override
    public boolean affectOwner(StructureStart structureStart, LivingEntity source) {
        if(source instanceof MobEntity mob){
            if(mob.isPersistent() || mob.cannotDespawn()){
                if(super.affectOwner(structureStart, source)){
                    if(!RestrictionsHelper.findPersistentMobInStructure(mob.getWorld(), structureStart, mob.getType())){
                        ((HasRestrictions) (Object) structureStart).setActiveRestrictions(false);
                    }

                    return true;
                }
            }
        }


        return false;
    }

}
