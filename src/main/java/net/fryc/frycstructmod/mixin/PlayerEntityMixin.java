package net.fryc.frycstructmod.mixin;


import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.fryc.frycstructmod.util.CanBeAffectedByStructure;
import net.fryc.frycstructmod.util.RestrictionsHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
abstract class PlayerEntityMixin extends LivingEntity implements CanBeAffectedByStructure {


    private boolean affectedByStructure = false;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }


    @ModifyReturnValue(method = "getBlockBreakingSpeed(Lnet/minecraft/block/BlockState;)F", at = @At("RETURN"))
    private float modifyMiningSpeedWhenAffectedByStructure(float original, BlockState block) {
        // executed on both client and server
        if(RestrictionsHelper.shouldModifyBlockBreakingSpeed(this, block)){
            return original * 0.01F;
        }
        return original;
    }

    public boolean isAffectedByStructure() {
        return this.affectedByStructure;
    }

    public void setAffectedByStructure(boolean affected) {
        this.affectedByStructure = affected;
    }
}
