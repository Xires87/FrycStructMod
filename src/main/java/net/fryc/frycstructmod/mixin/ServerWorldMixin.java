package net.fryc.frycstructmod.mixin;

import net.fryc.frycstructmod.util.ServerRestrictionsHelper;
import net.fryc.frycstructmod.util.interfaces.ControlsStructureTick;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
abstract class ServerWorldMixin implements ControlsStructureTick {

    private final HashMap<StructureStart, Integer> structureTickManager = new HashMap<>();


    @Inject(method = "tick(Ljava/util/function/BooleanSupplier;)V", at = @At("TAIL"))
    private void manageStructureTicking(BooleanSupplier shouldKeepTicking, CallbackInfo info){
        this.structureTickManager.entrySet().stream().filter(entry -> {
            return entry.getValue() > 0;
        }).forEach(entry -> {
            this.structureTickManager.put(entry.getKey(), entry.getValue() - 1);
        });
    }


    public boolean canTickStructure(StructureStart start){
        if(this.structureTickManager.containsKey(start)){
            if(this.structureTickManager.get(start) > 0){
                return false;
            }
        }

        this.structureTickManager.put(start, ServerRestrictionsHelper.STRUCTURE_TICK_DELAY);
        return true;
    }
}
