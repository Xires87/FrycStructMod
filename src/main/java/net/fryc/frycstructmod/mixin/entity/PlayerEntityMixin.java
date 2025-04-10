package net.fryc.frycstructmod.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.fryc.frycstructmod.structure.restrictions.AbstractStructureRestriction;
import net.fryc.frycstructmod.structure.restrictions.DefaultStructureRestriction;
import net.fryc.frycstructmod.structure.restrictions.StructureRestrictionInstance;
import net.fryc.frycstructmod.structure.restrictions.sources.events.SourceEntryEvent;
import net.fryc.frycstructmod.util.RestrictionsHelper;
import net.fryc.frycstructmod.util.ServerRestrictionsHelper;
import net.fryc.frycstructmod.util.interfaces.HoldsStructureStart;
import net.fryc.frycstructmod.util.interfaces.client.HoldsStructureRestrictionInstance;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PlayerEntity.class)
abstract class PlayerEntityMixin extends LivingEntity {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }


    @ModifyReturnValue(method = "getBlockBreakingSpeed(Lnet/minecraft/block/BlockState;)F", at = @At("RETURN"))
    private float modifyMiningSpeedWhenAffectedByStructure(float original, BlockState block) {
        // executed on both client and server
        Optional<AbstractStructureRestriction> optional = RestrictionsHelper.getRestrictionByTypeIfEntityIsAffectedByStructure("default", this);// TODO mozliwe ze bede musial jakos synchronizowac zarejestrowane restrykcje (chyba ze sie same synchronizuja bo nwm)
        if(optional.isPresent()){
            PlayerEntity dys = ((PlayerEntity) (Object) this);
            if(RestrictionsHelper.shouldPlayerBeAffectedByRestriction(optional.get(), dys)){
                return ((DefaultStructureRestriction) optional.get()).modifyBlockBreakingSpeedWhenNeeded(
                        original, block, dys
                );
            }
        }

        return original;
    }

    @Inject(method = "onKilledOther(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/LivingEntity;)Z", at = @At("HEAD"))
    private void onMobKill(ServerWorld world, LivingEntity killedEntity, CallbackInfoReturnable<Boolean> ret) {
        SourceEntryEvent.ON_MOB_KILL.triggerEvent(killedEntity, ((PlayerEntity)(Object)this), world, killedEntity.getBlockPos());
    }

}
