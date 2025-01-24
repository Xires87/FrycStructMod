package net.fryc.frycstructmod.util;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class RestrictionsHelper {

    public static boolean shouldModifyBlockBreakingSpeed(LivingEntity entity, BlockState block){
        // TODO dac rozne warunki co datapackami beda kontrolowane
        if(entity instanceof PlayerEntity player){
            if(((CanBeAffectedByStructure) player).isAffectedByStructure()){
                if(block.get(ModProperties.PLACED_BY_PLAYER) != null){
                    return !block.get(ModProperties.PLACED_BY_PLAYER);
                }
                return true;
            }
        }

        return false;
    }

    public static float modifyBlockBreakingSpeed(float speed){
        return speed * 0.01F;
    }
}
