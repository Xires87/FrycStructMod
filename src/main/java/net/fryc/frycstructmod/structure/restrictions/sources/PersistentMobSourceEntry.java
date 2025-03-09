package net.fryc.frycstructmod.structure.restrictions.sources;

import net.fryc.frycstructmod.util.RestrictionsHelper;
import net.fryc.frycstructmod.util.interfaces.HasRestrictions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;

public class PersistentMobSourceEntry extends LivingEntitySourceEntry {

    private final boolean forcePersistent;
    private final boolean checkForOtherPersistentEntities;
    public PersistentMobSourceEntry(Identifier sourceId, int sourceStrength, boolean forcePersistent, boolean checkForOtherPersistentEntities) {
        super(sourceId, sourceStrength);
        this.forcePersistent = forcePersistent;
        this.checkForOtherPersistentEntities = checkForOtherPersistentEntities;
    }

    @Override
    public boolean affectOwner(StructureStart structureStart, LivingEntity source) {
        if(source instanceof MobEntity mob){
            if(!this.shouldForcePersistent() || (mob.isPersistent() || mob.cannotDespawn())){
                if(super.affectOwner(structureStart, source)){
                    if(!this.shouldCheckForOtherPersistentEntities()){
                        if(!RestrictionsHelper.findPersistentMobInStructure(mob.getWorld(), structureStart, mob.getType(), this.shouldForcePersistent())){
                            ((HasRestrictions) (Object) structureStart).setActiveRestrictions(false);
                        }
                    }
                    else if(!source.getWorld().isClient()){
                        RestrictionsHelper.checkForPersistentEntitiesFromSource(
                                ((HasRestrictions) (Object) structureStart).getStructureRestrictionInstance(),
                                ((ServerWorld) source.getWorld()),
                                structureStart
                        );
                    }


                    return true;
                }
            }
        }


        return false;
    }

    public boolean shouldForcePersistent(){
        return this.forcePersistent;
    }

    public boolean shouldCheckForOtherPersistentEntities(){
        return this.checkForOtherPersistentEntities;
    }

}
