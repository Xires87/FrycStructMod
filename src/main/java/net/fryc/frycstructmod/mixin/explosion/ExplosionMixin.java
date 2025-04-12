package net.fryc.frycstructmod.mixin.explosion;

import net.fryc.frycstructmod.util.ServerRestrictionsHelper;
import net.fryc.frycstructmod.util.interfaces.HoldsStructureStart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Explosion.class)
abstract class ExplosionMixin implements HoldsStructureStart {

    private @Nullable StructureStart currentStructure = null;


    @Inject(method = "<init>(" +
                "Lnet/minecraft/world/World;" +
                "Lnet/minecraft/entity/Entity;" +
                "Lnet/minecraft/entity/damage/DamageSource;" +
                "Lnet/minecraft/world/explosion/ExplosionBehavior;" +
                "DDDFZ" +
                "Lnet/minecraft/world/explosion/Explosion$DestructionType;" +
            ")V", at = @At("TAIL"))
    private void addStructureStartIfExists(World world, @Nullable Entity entity, @Nullable DamageSource damageSource,
                                                 @Nullable ExplosionBehavior behavior, double x, double y, double z, float power,
                                                 boolean createFire, Explosion.DestructionType destructionType, CallbackInfo info) {

        if(!world.isClient()){
            BlockPos pos = BlockPos.ofFloored(x, y, z);
            ServerRestrictionsHelper.executeIfHasStructure(((ServerWorld) world), pos, structure -> {
                this.currentStructure = ((ServerWorld) world).getStructureAccessor().getStructureAt(pos, structure);
            });
        }
    }


    public @Nullable StructureStart getStructureStart() {
        return this.currentStructure;
    }

    public void setStructureStart(@Nullable StructureStart start) {
        this.currentStructure = start;
    }
}
