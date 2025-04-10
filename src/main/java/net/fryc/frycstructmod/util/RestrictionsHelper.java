package net.fryc.frycstructmod.util;


import net.fryc.frycstructmod.structure.restrictions.AbstractStructureRestriction;
import net.fryc.frycstructmod.structure.restrictions.DefaultStructureRestriction;
import net.fryc.frycstructmod.structure.restrictions.StructureRestrictionInstance;
import net.fryc.frycstructmod.structure.restrictions.registry.RestrictionRegistries;
import net.fryc.frycstructmod.structure.restrictions.sources.SourceEntry;
import net.fryc.frycstructmod.util.interfaces.CanBeAffectedByStructure;
import net.fryc.frycstructmod.util.interfaces.HoldsStructureStart;
import net.fryc.frycstructmod.util.interfaces.client.HoldsStructureRestrictionInstance;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;


public class RestrictionsHelper {

    public static boolean isPlacedByPlayer(BlockState block){
        if(block.get(ModProperties.PLACED_BY_PLAYER) != null){
            return block.get(ModProperties.PLACED_BY_PLAYER);
        }
        return false;
    }

    public static Optional<AbstractStructureRestriction> getRestrictionBySource(Collection<AbstractStructureRestriction> restrictions, SourceEntry<?> source){
        return restrictions.stream().filter(restriction -> {
            return restriction.getRestrictionSource().getEntries().contains(source);
        }).findFirst();
    }

    public static Optional<AbstractStructureRestriction> getRestrictionByType(String type, Identifier structureId){
        return structureId != null ? getRestrictionByType(type, structureId.toString()) : Optional.empty();
    }

    public static Optional<AbstractStructureRestriction> getRestrictionByType(String type, String structureId){
        HashMap<String, AbstractStructureRestriction> restrictions = RestrictionRegistries.STRUCTURE_RESTRICTIONS.get(structureId);
        if(restrictions != null){
            if(restrictions.containsKey(type)){
                return Optional.of(restrictions.get(type));
            }
        }

        return Optional.empty();
    }

    public static Optional<AbstractStructureRestriction> getRestrictionByTypeIfEntityIsAffectedByStructure(String type, LivingEntity entity){
        return ((CanBeAffectedByStructure) entity).isAffectedByStructure() ? getRestrictionByType(type, ((CanBeAffectedByStructure) entity).getStructureId()) : Optional.empty();
    }

    public static boolean shouldPlayerBeAffectedByRestriction(AbstractStructureRestriction restriction, PlayerEntity player){
        Optional<StructureRestrictionInstance> optional2 = player.getWorld().isClient() ?
                Optional.ofNullable(((HoldsStructureRestrictionInstance) player).getStructureRestrictionInstance()) :
                ServerRestrictionsHelper.getStructureRestrictionInstance(((HoldsStructureStart) player).getStructureStart());

        return optional2.filter(restrictionInstance -> !restrictionInstance.isRestrictionDisabled(restriction)).isPresent();
    }
}
