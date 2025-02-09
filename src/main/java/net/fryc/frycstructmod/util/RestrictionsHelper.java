package net.fryc.frycstructmod.util;


import net.fryc.frycstructmod.structure.restrictions.StructureRestriction;
import net.fryc.frycstructmod.structure.restrictions.registry.RestrictionRegistries;
import net.fryc.frycstructmod.structure.restrictions.sources.SourceEntry;
import net.fryc.frycstructmod.util.interfaces.CanBeAffectedByStructure;
import net.fryc.frycstructmod.util.interfaces.HoldsStructureStart;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;


public class RestrictionsHelper {

    // TODO dodac tu jeszcze event type zeby moglo byc wiele SourceType-ow ktore uzywaja tej samej klasy (np. LivingEntity)
    public static <T> void triggerEvent(Class<T> clazz, T source, PlayerEntity player, ServerWorld world, BlockPos pos){
        if(((CanBeAffectedByStructure) player).isAffectedByStructure()){
            StructureRestriction restriction = RestrictionRegistries.STRUCTURE_RESTRICTIONS.get(((CanBeAffectedByStructure) player).getStructureId());
            if(restriction != null){
                restriction.getRestrictionSource().getEntries().stream().filter(entry -> {
                    return entry.getEntryClass().equals(clazz);
                }).forEach(entry -> {
                    affectOwnerWithCorrectType(entry, clazz, source, player, world, pos);
                });
            }
        }
    }


    public static boolean isPlacedByPlayer(BlockState block){
        if(block.get(ModProperties.PLACED_BY_PLAYER) != null){
            return block.get(ModProperties.PLACED_BY_PLAYER);
        }
        return false;
    }

    public static <T> void affectOwnerWithCorrectType(SourceEntry<?> entry, Class<T> clazz, T source, PlayerEntity player, ServerWorld world, BlockPos pos){
        if(clazz.equals(LivingEntity.class)){
            affectOwnerWithLivingEntity((SourceEntry<LivingEntity>) entry, (LivingEntity) source, player, world, pos);
        }
        else if(clazz.equals(BlockState.class)){
            affectOwnerWithBlockState((SourceEntry<BlockState>) entry, (BlockState) source, player, world, pos);
        }
    }

    public static void affectOwnerWithLivingEntity(SourceEntry<LivingEntity> entry, LivingEntity killedEntity, PlayerEntity player, ServerWorld world, BlockPos pos){
        if(entry.affectOwner(world, ((HoldsStructureStart) player).getStructureStart(), killedEntity)){
            RestrictionsHelper.spawnSoulParticlesServerSided(world, pos);
        }
    }

    public static void affectOwnerWithBlockState(SourceEntry<BlockState> entry, BlockState destroyedBlock, PlayerEntity player, ServerWorld world, BlockPos pos){
        if(entry.affectOwner(world, ((HoldsStructureStart) player).getStructureStart(), destroyedBlock)){
            RestrictionsHelper.spawnSoulParticlesServerSided(world, pos);
        }
    }

    public static void spawnSoulParticlesServerSided(ServerWorld world, BlockPos pos){
        world.addParticle(ParticleTypes.SOUL, true, pos.getX(), pos.getY(), pos.getZ(), 0d, 5d, 0d);
        // TODO dac wiecej tych duszkow i zrobic losowanie pozycji (i networking do nich trzeba bo to na serwerze jest odpalane)
    }
}
