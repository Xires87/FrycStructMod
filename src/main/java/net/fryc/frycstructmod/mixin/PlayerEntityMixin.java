package net.fryc.frycstructmod.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.fryc.frycstructmod.structure.restrictions.AbstractStructureRestriction;
import net.fryc.frycstructmod.structure.restrictions.DefaultStructureRestriction;
import net.fryc.frycstructmod.structure.restrictions.registry.RestrictionRegistries;
import net.fryc.frycstructmod.structure.restrictions.sources.events.SourceEntryEvent;
import net.fryc.frycstructmod.util.interfaces.CanBeAffectedByStructure;
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

@Mixin(PlayerEntity.class)
abstract class PlayerEntityMixin extends LivingEntity implements CanBeAffectedByStructure {

    private String affectedByStructure = "";

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }


    @ModifyReturnValue(method = "getBlockBreakingSpeed(Lnet/minecraft/block/BlockState;)F", at = @At("RETURN"))
    private float modifyMiningSpeedWhenAffectedByStructure(float original, BlockState block) {
        // executed on both client and server
        if(this.isAffectedByStructure()){
            AbstractStructureRestriction restriction = RestrictionRegistries.STRUCTURE_RESTRICTIONS.get(this.getStructureId());
            if(restriction != null){
                if(restriction instanceof DefaultStructureRestriction defRestriction)
                return defRestriction.modifyBlockBreakingSpeedWhenNeeded(original, block, ((PlayerEntity)(Object)this));
            }
        }
        return original;
    }

    @Inject(method = "onKilledOther(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/LivingEntity;)Z", at = @At("HEAD"))
    private void onMobKill(ServerWorld world, LivingEntity killedEntity, CallbackInfoReturnable<Boolean> ret) {
        SourceEntryEvent.ON_MOB_KILL.triggerEvent(killedEntity, ((PlayerEntity)(Object)this), world, killedEntity.getBlockPos());
    }

    public boolean isAffectedByStructure() {
        return !this.affectedByStructure.isEmpty();
    }

    public void setAffectedByStructure(String affected) {
        this.affectedByStructure = affected;
    }

    public String getStructureId(){
        return this.affectedByStructure;
    }
}
