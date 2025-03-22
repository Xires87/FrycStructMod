package net.fryc.frycstructmod.structure.restrictions.sources.events;

import net.fryc.frycstructmod.structure.restrictions.StructureRestrictionInstance;
import net.fryc.frycstructmod.structure.restrictions.sources.SourceEntry;
import net.fryc.frycstructmod.util.ServerRestrictionsHelper;
import net.fryc.frycstructmod.util.interfaces.CanBeAffectedByStructure;
import net.fryc.frycstructmod.util.interfaces.HasRestrictions;
import net.fryc.frycstructmod.util.interfaces.HoldsStructureStart;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class SourceEntryEvent<T> implements Event {

    public static final SourceEntryEvent<LivingEntity> ON_MOB_KILL = new SourceEntryEvent<>();
    public static final SourceEntryEvent<BlockState> ON_BLOCK_DESTROY = new SourceEntryEvent<>();
    public static final SourceEntryEvent<ItemStack> ON_ITEM_USE_FINISH = new SourceEntryEvent<>();


    public void triggerEvent(T source, PlayerEntity player, ServerWorld world, BlockPos pos){
        if(((CanBeAffectedByStructure) player).isAffectedByStructure()){
            StructureRestrictionInstance instance = ((HasRestrictions)(Object) ((HoldsStructureStart) player).getStructureStart()).getStructureRestrictionInstance();
            if(instance != null){
                instance.getActiveRestrictions().forEach(res -> {
                    res.getRestrictionSource().getEntries().stream().filter(entry -> {
                        return entry.getEvent().equals(this);
                    }).forEach(entry -> {
                        if(((SourceEntry<T>) entry).affectOwner(world, ((HoldsStructureStart) player).getStructureStart(), source, player)){
                            ServerRestrictionsHelper.spawnSoulParticlesServerSided(world, pos);
                        }
                    });
                });
            }
        }
    }

}
