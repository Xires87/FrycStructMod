package net.fryc.frycstructmod.mixin;


import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.fryc.frycstructmod.structure.restrictions.AbstractStructureRestriction;
import net.fryc.frycstructmod.structure.restrictions.StructureRestrictionInstance;
import net.fryc.frycstructmod.structure.restrictions.registry.RestrictionRegistries;
import net.fryc.frycstructmod.util.ServerRestrictionsHelper;
import net.fryc.frycstructmod.util.interfaces.HasRestrictions;
import net.fryc.frycstructmod.util.interfaces.PlayerLocator;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiecesList;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.Structure;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;

@Mixin(StructureStart.class)
abstract class StructureStartMixin implements HasRestrictions, PlayerLocator {

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
        ServerRestrictionsHelper.onStructureStartSaveToNbt(this, original, context, chunkPos);

        return original;
    }

    @ModifyReturnValue(method = "fromNbt(Lnet/minecraft/structure/StructureContext;Lnet/minecraft/nbt/NbtCompound;J)Lnet/minecraft/structure/StructureStart;", at = @At("RETURN"))
    private static StructureStart loadRestrictionsActiveFromNbt(StructureStart original, StructureContext context, NbtCompound nbt, long seed) {
        ServerRestrictionsHelper.onStructureStartLoadFromNbt(original, context, nbt, seed);

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

    public boolean createStructureRestrictionInstance(DynamicRegistryManager manager){
        Identifier id = manager.get(RegistryKeys.STRUCTURE).getId(((StructureStart) (Object) this).getStructure());
        if(id != null){
            HashMap<String, AbstractStructureRestriction> restrictions = RestrictionRegistries.STRUCTURE_RESTRICTIONS.get(id.toString());
            if(restrictions != null){
                this.structureRestrictionInstance = new StructureRestrictionInstance(restrictions.values());
                return true;
            }
        }

        this.setActiveRestrictions(false);
        return false;
    }

    public List<PlayerEntity> getPlayersInStructure(World world){
        return world.getEntitiesByType(EntityType.PLAYER, Box.from(((StructureStart)(Object)this).getBoundingBox()), LivingEntity::isAlive);
    }
}
