package net.fryc.frycstructmod.mixin;


import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.fryc.frycstructmod.structure.restrictions.AbstractStructureRestriction;
import net.fryc.frycstructmod.structure.restrictions.StructureRestrictionInstance;
import net.fryc.frycstructmod.structure.restrictions.registry.RestrictionRegistries;
import net.fryc.frycstructmod.util.interfaces.HasRestrictions;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiecesList;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.structure.Structure;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;

@Mixin(StructureStart.class)
abstract class StructureStartMixin implements HasRestrictions {

    private boolean restrictionsActive = false;

    @Nullable
    private StructureRestrictionInstance structureRestrictionInstance = null;

    @Inject(method = "<init>(Lnet/minecraft/world/gen/structure/Structure;Lnet/minecraft/util/math/ChunkPos;ILnet/minecraft/structure/StructurePiecesList;)V", at = @At("TAIL"))
    private void addStructureRestrictionInstance(Structure structure, ChunkPos pos, int references, StructurePiecesList children, CallbackInfo info) {
        if(structure != null){
            this.restrictionsActive = true;
        }
    }

    @ModifyReturnValue(method = "toNbt(Lnet/minecraft/structure/StructureContext;Lnet/minecraft/util/math/ChunkPos;)Lnet/minecraft/nbt/NbtCompound;", at = @At("RETURN"))
    private NbtCompound saveRestrictionsActiveToNbt(NbtCompound original, StructureContext context, ChunkPos chunkPos) {
        if(original.contains("Children")){
            original.putBoolean("structureRestrictionActive", this.hasActiveRestrictions());
            if(this.getStructureRestrictionInstance() != null){
                original.putInt("structureRestrictionInstancePower", this.getStructureRestrictionInstance().getCurrentPower());
            }
        }
        return original;
    }

    @ModifyReturnValue(method = "fromNbt(Lnet/minecraft/structure/StructureContext;Lnet/minecraft/nbt/NbtCompound;J)Lnet/minecraft/structure/StructureStart;", at = @At("RETURN"))
    private static StructureStart loadRestrictionsActiveFromNbt(StructureStart original, StructureContext context, NbtCompound nbt, long seed) {
        if(nbt.contains("structureRestrictionActive")){
            if(original != null){
                if(!original.equals(StructureStart.DEFAULT)){
                    HasRestrictions str = ((HasRestrictions)(Object) original);
                    boolean active = nbt.getBoolean("structureRestrictionActive");

                    str.setActiveRestrictions(active);
                    if(active){
                        if(nbt.contains("structureRestrictionInstancePower")){
                            str.createStructureRestrictionInstance(context.registryManager());
                            if(str.getStructureRestrictionInstance() != null){
                                str.getStructureRestrictionInstance().setCurrentPower(nbt.getInt("structureRestrictionInstancePower"));
                            }
                        }
                    }
                }
            }
        }

        return original;
    }


    public boolean hasActiveRestrictions(){
        return this.restrictionsActive;
    }

    public void setActiveRestrictions(boolean active){
        this.restrictionsActive = active;
    }

    @Nullable
    public StructureRestrictionInstance getStructureRestrictionInstance(){
        return this.structureRestrictionInstance;
    }

    public void createStructureRestrictionInstance(DynamicRegistryManager manager){
        Identifier id = manager.get(RegistryKeys.STRUCTURE).getId(((StructureStart) (Object) this).getStructure());
        if(id != null){
            HashMap<String, AbstractStructureRestriction> restrictions = RestrictionRegistries.STRUCTURE_RESTRICTIONS.get(id.toString());
            if(restrictions != null){
                this.structureRestrictionInstance = new StructureRestrictionInstance(restrictions.values());
            }
            else {
                this.setActiveRestrictions(false);
            }

        }
    }
}
