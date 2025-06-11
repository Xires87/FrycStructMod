package net.fryc.frycstructmod.mixin.explosion;

import net.fryc.frycstructmod.structure.restrictions.StructureRestrictionInstance;
import net.fryc.frycstructmod.structure.restrictions.registry.RestrictionTypes;
import net.fryc.frycstructmod.util.RestrictionsHelper;
import net.fryc.frycstructmod.util.ServerRestrictionsHelper;
import net.fryc.frycstructmod.util.interfaces.HasRestrictions;
import net.fryc.frycstructmod.util.interfaces.HoldsStructureStart;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.explosion.EntityExplosionBehavior;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(EntityExplosionBehavior.class)
abstract class EntityExplosionBehaviorMixin {

    @Inject(method = "canDestroyBlock(" +
                "Lnet/minecraft/world/explosion/Explosion;" +
                "Lnet/minecraft/world/BlockView;" +
                "Lnet/minecraft/util/math/BlockPos;" +
                "Lnet/minecraft/block/BlockState;" +
                "F" +
            ")Z", at = @At("RETURN"), cancellable = true)
    private void disallowDestroyingWhenNeeded(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float power, CallbackInfoReturnable<Boolean> ret) {
        if(world instanceof ServerWorld serverWorld){
            StructureStart start = ((HoldsStructureStart) explosion).getStructureStart();
            if(start != null){
                if(((HasRestrictions) (Object) start).hasActiveRestrictions()){
                    RestrictionsHelper.getRestrictionByType(
                            RestrictionTypes.EXPLOSION, serverWorld.getRegistryManager().get(RegistryKeys.STRUCTURE).getId(start.getStructure())
                    ).ifPresent(restriction -> {
                        Optional<StructureRestrictionInstance> opt = ServerRestrictionsHelper.getStructureRestrictionInstance(start);
                        opt.ifPresent(restrictionInstance -> {
                            if(!restrictionInstance.isRestrictionDisabled(restriction)){
                                ret.setReturnValue(false);
                            }
                        });
                    });
                }
            }
        }
    }
}
