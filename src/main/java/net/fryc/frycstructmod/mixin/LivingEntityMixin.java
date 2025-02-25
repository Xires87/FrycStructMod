package net.fryc.frycstructmod.mixin;

import net.fryc.frycstructmod.util.RestrictionsHelper;
import net.fryc.frycstructmod.util.interfaces.HasRestrictions;
import net.minecraft.entity.Attackable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin extends Entity implements Attackable {

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    // checks for persistent mobs when persistent mob dies without player's help
    @Inject(method = "onDeath(Lnet/minecraft/entity/damage/DamageSource;)V", at = @At("HEAD"))
    private void onPersistentMobDeath(DamageSource source, CallbackInfo info) {
        if(!this.getWorld().isClient()){
            if(((LivingEntity)(Object)this) instanceof MobEntity mob){
                if(mob.isPersistent() || mob.cannotDespawn()){
                    if(mob.getPrimeAdversary() == null || !mob.getPrimeAdversary().isPlayer()){
                        RestrictionsHelper.executeIfHasStructure(((ServerWorld) mob.getWorld()), mob.getBlockPos(), structure -> {

                            StructureStart start = ((ServerWorld) mob.getWorld()).getStructureAccessor().getStructureAt(this.getBlockPos(), structure);
                            HasRestrictions startWithRestrictions = ((HasRestrictions) (Object) start);

                            if(startWithRestrictions.hasActiveRestrictions()){
                                RestrictionsHelper.checkForPersistentEntitiesOnEnter(
                                        startWithRestrictions.getStructureRestrictionInstance(),
                                        ((ServerWorld) mob.getWorld()),
                                        start
                                );
                            }
                        });
                    }
                }
            }
        }
    }
}
