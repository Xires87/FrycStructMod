package net.fryc.frycstructmod.util;


import net.minecraft.block.BlockState;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;


public class RestrictionsHelper {

    public static boolean isPlacedByPlayer(BlockState block){
        if(block.get(ModProperties.PLACED_BY_PLAYER) != null){
            return block.get(ModProperties.PLACED_BY_PLAYER);
        }
        return false;
    }

    public static void spawnSoulParticlesServerSided(ServerWorld world, BlockPos pos){
        world.addParticle(ParticleTypes.SOUL, true, pos.getX(), pos.getY(), pos.getZ(), 0d, 5d, 0d);
        // TODO dac wiecej tych duszkow i zrobic losowanie pozycji (i networking do nich trzeba bo to na serwerze jest odpalane)
    }
}
