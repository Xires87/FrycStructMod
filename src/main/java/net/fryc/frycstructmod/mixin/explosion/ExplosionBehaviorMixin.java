package net.fryc.frycstructmod.mixin.explosion;

import net.fryc.frycstructmod.structure.restrictions.StructureRestrictionInstance;
import net.fryc.frycstructmod.util.interfaces.HasRestrictions;
import net.fryc.frycstructmod.util.interfaces.HoldsStructureStart;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExplosionBehavior.class)
abstract class ExplosionBehaviorMixin {

    @Inject(method = "canDestroyBlock(" +
                "Lnet/minecraft/world/explosion/Explosion;" +
                "Lnet/minecraft/world/BlockView;" +
                "Lnet/minecraft/util/math/BlockPos;" +
                "Lnet/minecraft/block/BlockState;" +
                "F" +
            ")Z", at = @At("RETURN"), cancellable = true)
    private void disallowDestroyingWhenNeeded(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float power, CallbackInfoReturnable<Boolean> ret) {
        if(((HoldsStructureStart) explosion).getStructureStart() != null){
            StructureRestrictionInstance strResInstance = ((HasRestrictions) (Object) ((HoldsStructureStart) explosion).getStructureStart()).getStructureRestrictionInstance();
            if(strResInstance != null){
                ret.setReturnValue(false);// TODO dac tu checka na restrykcje i wtedy blokowac
            }
        }
    }
}
