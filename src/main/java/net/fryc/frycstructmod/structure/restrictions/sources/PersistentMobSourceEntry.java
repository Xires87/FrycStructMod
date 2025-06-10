package net.fryc.frycstructmod.structure.restrictions.sources;

import net.fryc.frycstructmod.structure.restrictions.StructureRestrictionInstance;
import net.fryc.frycstructmod.util.RestrictionsHelper;
import net.fryc.frycstructmod.util.ServerRestrictionsHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class PersistentMobSourceEntry extends LivingEntitySourceEntry {

    private final boolean forcePersistent;
    private final boolean checkForOtherPersistentEntities;
    public PersistentMobSourceEntry(Identifier sourceId, int sourceStrength, boolean forcePersistent, boolean checkForOtherPersistentEntities) {
        super(sourceId, sourceStrength);
        this.forcePersistent = forcePersistent;
        this.checkForOtherPersistentEntities = checkForOtherPersistentEntities;
    }

    @Override
    public boolean affectOwner(StructureStart structureStart, LivingEntity source, PlayerEntity player) {
        if(source instanceof MobEntity mob){
            if(!this.shouldForcePersistent() || (mob.isPersistent() || mob.cannotDespawn())){
                if(super.affectOwner(structureStart, source, player)){
                    Optional<StructureRestrictionInstance> optional = ServerRestrictionsHelper.getStructureRestrictionInstance(structureStart);
                    if(optional.isPresent()){
                        StructureRestrictionInstance instance = optional.get();
                        if(this.owner.isShared()){
                            this.checkAndUpdateSharedRestrictions(instance, structureStart, ((ServerWorld) source.getWorld()), source.getType());
                        }
                        else {
                            this.checkAndUpdateSeparateRestrictions(instance, structureStart, ((ServerWorld) source.getWorld()), source.getType());
                        }
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

    public void checkAndUpdateSharedRestrictions(StructureRestrictionInstance instance, StructureStart structureStart, ServerWorld world, EntityType<?> entityType){
        if(this.shouldCheckForOtherPersistentEntities()){
            ServerRestrictionsHelper.checkForPersistentEntitiesFromSharedSourceAndUpdate(instance, world, structureStart);
        }
        else {
            if(!RestrictionsHelper.findPersistentMob(world, structureStart.getBoundingBox(), entityType, this.shouldForcePersistent())){
                instance.getActiveRestrictions().stream().filter(restriction -> {
                    return restriction.getRestrictionSource().isShared();
                }).toList().forEach(instance::disableRestriction);

                ServerRestrictionsHelper.tryToRemoveRestrictionsFromStructure(structureStart, instance);
            }
        }
    }

    public void checkAndUpdateSeparateRestrictions(StructureRestrictionInstance instance, StructureStart structureStart, ServerWorld world, EntityType<?> entityType){
        if(this.shouldCheckForOtherPersistentEntities()){
            ServerRestrictionsHelper.checkForPersistentEntitiesFromSeparateSourceAndUpdate(this.owner, instance, world, structureStart);
        }
        else {
            if(!RestrictionsHelper.findPersistentMob(world, structureStart.getBoundingBox(), entityType, this.shouldForcePersistent())){
                RestrictionsHelper.getRestrictionBySource(instance.getActiveRestrictions(), this).ifPresent(instance::disableRestriction);
            }
        }
    }

    public static boolean isOwnerShared(PersistentMobSourceEntry entry){
        return entry.owner.isShared();
    }

}
