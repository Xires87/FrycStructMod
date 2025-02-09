package net.fryc.frycstructmod.util;


import net.fryc.frycstructmod.structure.restrictions.StructureRestrictionInstance;
import net.fryc.frycstructmod.structure.restrictions.sources.PersistentMobSourceEntry;
import net.fryc.frycstructmod.util.interfaces.HasRestrictions;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;


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

    public static boolean findPersistentMobInStructure(World world, StructureStart structureStart, EntityType<?> type) {
        return !world.getEntitiesByType(type, Box.from(structureStart.getBoundingBox()), entity -> {
            return entity.isAlive() && entity instanceof MobEntity mob && (mob.isPersistent() || mob.cannotDespawn());
        }).isEmpty();
    }

    public static void checkForPersistentEntitiesOnEnter(StructureRestrictionInstance restrictionInstance, ServerWorld world, StructureStart start){
        restrictionInstance.getStructureRestriction().getRestrictionSource().getEntries().stream().filter(entry -> {
            return entry instanceof PersistentMobSourceEntry;
        }).toList().forEach(entry -> {
            EntityType.get(((PersistentMobSourceEntry) entry).getSourceId().toString()).ifPresent(type -> {
                if(!RestrictionsHelper.findPersistentMobInStructure(world, start, type)){
                    ((HasRestrictions) (Object) start).setActiveRestrictions(false);
                }
            });
        });
    }
}
