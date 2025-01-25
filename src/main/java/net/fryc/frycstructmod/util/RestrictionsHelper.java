package net.fryc.frycstructmod.util;

import net.fryc.frycstructmod.structure.RegisteredRestrictions;
import net.fryc.frycstructmod.structure.StructureRestriction;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class RestrictionsHelper {



    public static boolean shouldModifyBlockBreakingSpeed(LivingEntity entity, BlockState block){
        // TODO dac rozne warunki co datapackami beda kontrolowane
        //TODO przeniesc to do restriction (i mocno zmodyfikowac)
        if(entity instanceof PlayerEntity player){
            if(((CanBeAffectedByStructure) player).isAffectedByStructure()){
                StructureRestriction restriction = RegisteredRestrictions.STRUCTURE_RESTRICTIONS.get(((CanBeAffectedByStructure) player).getStructureId());
                if(restriction != null){
                    if(restriction.isMiningAllowedWhenBlockIsPlacedByPlayer() && isPlacedByPlayer(block)){
                        return false;
                    }
                    if(restriction.isMiningAllowed()){
                        return restriction.getMiningExcludedBlocks().contains(block.getBlock());
                    }
                    else {
                        return !restriction.getMiningExcludedBlocks().contains(block.getBlock());
                    }
                }
                return false;
            }
        }

        return false;
    }

    public static float modifyBlockBreakingSpeed(float speed){
        return speed * 0.01F;
    }

    public static boolean isPlacedByPlayer(BlockState block){
        if(block.get(ModProperties.PLACED_BY_PLAYER) != null){
            return block.get(ModProperties.PLACED_BY_PLAYER);
        }
        return false;
    }
}
