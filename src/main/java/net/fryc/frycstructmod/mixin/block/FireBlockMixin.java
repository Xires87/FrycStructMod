package net.fryc.frycstructmod.mixin.block;

import net.fryc.frycstructmod.util.RestrictionsHelper;
import net.fryc.frycstructmod.util.ServerRestrictionsHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireBlock.class)
abstract class FireBlockMixin {

    @Inject(method = "scheduledTick(" +
                "Lnet/minecraft/block/BlockState;" +
                "Lnet/minecraft/server/world/ServerWorld;" +
                "Lnet/minecraft/util/math/BlockPos;" +
                "Lnet/minecraft/util/math/random/Random;" +
            ")V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;scheduleBlockTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;I)V", shift = At.Shift.AFTER), cancellable = true)
    private void removeFireBlockTicksInStructures(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo info){
        ServerRestrictionsHelper.executeIfHasStructure(world, pos, structure -> {
            RestrictionsHelper.getRestrictionByType("fire", world.getRegistryManager().get(RegistryKeys.STRUCTURE).getId(structure)).ifPresent(restriction -> {
                restriction.executeWhenEnabled(world, pos, structure, (start, restrictionInstance) -> {
                    info.cancel();// TODO sprawdzic tutaj czy na tym bloku nie powinno tickowac i wtedy cancelowac (i jeszcze dac opcje do usuwania ognia, ktory nie znika jak tickow nie ma)

                    //world.removeBlock(pos, false); // TODO dac to jesli powinno usuwac ogien
                });
            });
        });
    }
}
