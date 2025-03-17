package net.fryc.frycstructmod.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.fryc.frycstructmod.structure.restrictions.AbstractStructureRestriction;
import net.fryc.frycstructmod.structure.restrictions.DefaultStructureRestriction;
import net.fryc.frycstructmod.structure.restrictions.sources.events.SourceEntryEvent;
import net.fryc.frycstructmod.util.RestrictionsHelper;
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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Mixin(PlayerEntity.class)
abstract class PlayerEntityMixin extends LivingEntity implements CanBeAffectedByStructure {

    private String affectedByStructure = "";
    private final Set<String> restrictionsImmuneTo = new HashSet<>();

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }


    @ModifyReturnValue(method = "getBlockBreakingSpeed(Lnet/minecraft/block/BlockState;)F", at = @At("RETURN"))
    private float modifyMiningSpeedWhenAffectedByStructure(float original, BlockState block) {
        // executed on both client and server
        Optional<AbstractStructureRestriction> optional = RestrictionsHelper.getRestrictionByTypeIfAffectsPlayer("default", ((PlayerEntity)(Object) this));
        return optional.map(abstractStructureRestriction -> ((DefaultStructureRestriction) abstractStructureRestriction).modifyBlockBreakingSpeedWhenNeeded(
                original, block, ((PlayerEntity) (Object) this)
        )).orElse(original);
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

    public Set<String> getRestrictionsImmuneTo(){
        return this.restrictionsImmuneTo;
    }

    public boolean shouldBeAffectedByRestriction(String restrictionType){
        return !this.getRestrictionsImmuneTo().contains(restrictionType);
    }
}
