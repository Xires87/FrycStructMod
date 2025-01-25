package net.fryc.frycstructmod.util;


import net.minecraft.block.BlockState;


public class RestrictionsHelper {




    public static boolean isPlacedByPlayer(BlockState block){
        if(block.get(ModProperties.PLACED_BY_PLAYER) != null){
            return block.get(ModProperties.PLACED_BY_PLAYER);
        }
        return false;
    }
}
