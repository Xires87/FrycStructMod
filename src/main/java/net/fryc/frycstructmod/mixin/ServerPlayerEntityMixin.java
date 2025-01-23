package net.fryc.frycstructmod.mixin;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.fryc.frycstructmod.util.HasRestrictions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ServerPlayerEntity.class)
abstract class ServerPlayerEntityMixin extends PlayerEntity {

    private int delay = 30;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void test(CallbackInfo info) {

        if(--this.delay < 1){
            ServerWorld world = ((ServerWorld) this.getWorld());
            if(world.getStructureAccessor().hasStructureReferences(this.getBlockPos())){
                Map<Structure, LongSet> structureMap = world.getChunk(this.getBlockPos()).getStructureReferences();

                structureMap.keySet().forEach((structure) -> {
                    StructureStart start = world.getStructureAccessor().getStructureAt(this.getBlockPos(), structure);
                    if(start != StructureStart.DEFAULT){
                        if(((HasRestrictions) (Object) start).hasActiveRestrictions()){
                            this.sendMessage(Text.of("Weszlem do struktury"));
                            ((HasRestrictions) (Object) start).setRestrictions(false);
                        }
                    }
                });
            }


            this.delay = 30;
        }


    }
}
