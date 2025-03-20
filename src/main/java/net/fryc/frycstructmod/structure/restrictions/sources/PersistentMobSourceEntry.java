package net.fryc.frycstructmod.structure.restrictions.sources;

import net.fryc.frycstructmod.structure.restrictions.StructureRestrictionInstance;
import net.fryc.frycstructmod.util.ServerRestrictionsHelper;
import net.fryc.frycstructmod.util.interfaces.HasRestrictions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
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
// TODO naprawic persistent entity (checkForOtherPersistentEntities powinno patrzec tylko na SWOJE sourcey jak jest separate albo na wsystkie SHARED sourcey jak shared jest)
    @Override
    public boolean affectOwner(StructureStart structureStart, LivingEntity source, PlayerEntity player) {
        if(source instanceof MobEntity mob){
            if(!this.shouldForcePersistent() || (mob.isPersistent() || mob.cannotDespawn())){
                if(super.affectOwner(structureStart, source, player)){
                    StructureRestrictionInstance instance = ((HasRestrictions) (Object) structureStart).getStructureRestrictionInstance();
                    if(!this.shouldCheckForOtherPersistentEntities()){
                        if(!ServerRestrictionsHelper.findPersistentMobInStructure(mob.getWorld(), structureStart, mob.getType(), this.shouldForcePersistent())){
                            instance.updateDisabledRestrictions();
                            ServerRestrictionsHelper.tryToRemoveRestrictionsFromStructure(structureStart, instance);
                        }
                    }
                    else if(!source.getWorld().isClient()){
                        ServerRestrictionsHelper.checkForPersistentEntitiesFromSource(
                                instance,
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
