package net.fryc.frycstructmod.mixin.entity;

import com.mojang.authlib.GameProfile;
import net.fryc.frycstructmod.util.ServerRestrictionsHelper;
import net.fryc.frycstructmod.util.interfaces.CanBeAffectedByStructure;
import net.fryc.frycstructmod.util.interfaces.HoldsStructureStart;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
abstract class ServerPlayerEntityMixin extends PlayerEntity implements CanBeAffectedByStructure, HoldsStructureStart {

    @Nullable
    private StructureStart currentStructure = null;

    private int delay = ServerRestrictionsHelper.PLAYER_JOIN_STRUCTURE_TICK_DELAY;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void checkForStructureEnter(CallbackInfo info) {
        if(--this.delay < 1){
            ServerWorld world = ((ServerWorld) this.getWorld());
            ServerRestrictionsHelper.executeIfHasStructureOrElse(world, this.getBlockPos(), structure -> {
                ServerRestrictionsHelper.tickStructure(this, structure);
            }, () -> ServerRestrictionsHelper.resetCurrentStructureWhenNeeded(this));

            this.delay = ServerRestrictionsHelper.STRUCTURE_TICK_DELAY;
        }
    }


    public @Nullable StructureStart getStructureStart(){
        return this.currentStructure;
    }

    public void setStructureStart(@Nullable StructureStart start){
        this.currentStructure = start;
    }
}
