package net.fryc.frycstructmod.util;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fryc.frycstructmod.network.ModPackets;
import net.fryc.frycstructmod.structure.restrictions.AbstractStructureRestriction;
import net.fryc.frycstructmod.structure.restrictions.StructureRestrictionInstance;
import net.fryc.frycstructmod.structure.restrictions.sources.PersistentMobSourceEntry;
import net.fryc.frycstructmod.structure.restrictions.sources.SourceEntry;
import net.fryc.frycstructmod.util.interfaces.CanBeAffectedByStructure;
import net.fryc.frycstructmod.util.interfaces.HasRestrictions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.Structure;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class ServerRestrictionsHelper {

    public static void setAffectedByStructureServerAndClient(PlayerEntity player, String affected, StructureRestrictionInstance instance) {
        ((CanBeAffectedByStructure) player).setAffectedByStructure(affected);
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(affected);
        if(instance != null){
            buf.writeInt(instance.getCurrentSharedPower());
            for(AbstractStructureRestriction res : instance.getCurrentSeperatePowers().keySet()){
                buf.writeString(res.getRestrictionType());
                buf.writeInt(instance.getCurrentSeperatePowers().get(res));
            }
        }
        ServerPlayNetworking.send(((ServerPlayerEntity) player), ModPackets.AFFECT_BY_STRUCTURE, buf);
    }

    public static Optional<StructureRestrictionInstance> getStructureRestrictionInstance(StructureStart start){
        return Optional.ofNullable(((HasRestrictions)(Object) start).getStructureRestrictionInstance());
    }

    public static void spawnSoulParticlesServerSided(ServerWorld world, BlockPos pos){
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        for (ServerPlayerEntity pl : PlayerLookup.tracking(world, pos)) {
            ServerPlayNetworking.send(pl, ModPackets.SPAWN_SOUL_PARTICLES, buf);
        }
    }

    public static boolean findPersistentMobInStructure(World world, StructureStart structureStart, EntityType<?> type, boolean shouldForcePersistent) {
        return !world.getEntitiesByType(type, Box.from(structureStart.getBoundingBox()), entity -> {
            return entity.isAlive() && (!shouldForcePersistent || (entity instanceof MobEntity mob && (mob.isPersistent() || mob.cannotDespawn())));
        }).isEmpty();
    }

    public static void checkForPersistentEntitiesFromSource(StructureRestrictionInstance restrictionInstance, ServerWorld world, StructureStart start){
        if(restrictionInstance != null){
            for(AbstractStructureRestriction restriction : restrictionInstance.getStructureRestrictions()){
                List<SourceEntry<?>> list = restriction.getRestrictionSource().getEntries().stream().filter(entry -> {
                    return entry instanceof PersistentMobSourceEntry;
                }).toList();
                boolean shouldDisable = !list.isEmpty();
                for(SourceEntry<?> entry : list){
                    Optional<EntityType<?>> type = EntityType.get(((PersistentMobSourceEntry) entry).getSourceId().toString());
                    if(type.isPresent()){
                        if(ServerRestrictionsHelper.findPersistentMobInStructure(world, start, type.get(), ((PersistentMobSourceEntry) entry).shouldForcePersistent())){
                            shouldDisable = false;
                            break;
                        }
                    }
                }

                if(shouldDisable){
                    restrictionInstance.disableRestriction(restriction);
                }
            }

            tryToRemoveRestrictionsFromStructure(start, restrictionInstance);
        }
    }

    /**
     *  Returns true after removing restrictions from structure
     */
    public static boolean tryToRemoveRestrictionsFromStructure(StructureStart start, StructureRestrictionInstance restrictionInstance){
        if(!restrictionInstance.isActive()){
            ((HasRestrictions) (Object) start).setActiveRestrictions(false);

            return true;
        }

        return false;
    }

    public static void executeIfHasStructureOrElse(ServerWorld world, BlockPos pos, Consumer<Structure> presentAction, Runnable elseAction){
        if(world.getStructureAccessor().hasStructureReferences(pos)){
            Map<Structure, LongSet> structureMap = world.getChunk(pos).getStructureReferences();

            structureMap.keySet().stream().filter(structure -> {
                return world.getStructureAccessor().getStructureAt(pos, structure) != StructureStart.DEFAULT;
            }).findFirst().ifPresentOrElse(presentAction, elseAction);
        }
        else {
            elseAction.run();
        }
    }

    public static void executeIfHasStructure(ServerWorld world, BlockPos pos, Consumer<Structure> action){
        executeIfHasStructureOrElse(world, pos, action, () -> {});
    }

    public static void onStructureStartLoadFromNbt(StructureStart start, StructureContext context, NbtCompound nbt, long seed){
        if(nbt.contains("structureRestrictionActive")){
            if(start != null){
                if(!start.equals(StructureStart.DEFAULT)){
                    HasRestrictions str = ((HasRestrictions)(Object) start);
                    boolean active = nbt.getBoolean("structureRestrictionActive");

                    str.setActiveRestrictions(active);
                    if(active){
                        if(nbt.contains("structureRestrictionInstancePower")){
                            str.createStructureRestrictionInstance(context.registryManager());
                            if(str.getStructureRestrictionInstance() != null){
                                str.getStructureRestrictionInstance().setCurrentSharedPower(nbt.getInt("structureRestrictionInstancePower"));
                                for (Map.Entry<AbstractStructureRestriction, Integer> entry : str.getStructureRestrictionInstance().getCurrentSeperatePowers().entrySet()) {
                                    if (nbt.contains("structureRestrictionInstancePower" + entry.getKey().getRestrictionType())) {
                                        str.getStructureRestrictionInstance().getCurrentSeperatePowers().put(entry.getKey(), nbt.getInt("structureRestrictionInstancePower" + entry.getKey().getRestrictionType()));
                                    }
                                }

                                str.getStructureRestrictionInstance().updateDisabledRestrictions();
                            }
                        }
                    }
                }
            }
        }
    }

    public static void onStructureStartSaveToNbt(HasRestrictions structureStart, NbtCompound nbtCompound, StructureContext context, ChunkPos chunkPos){
        if(nbtCompound.contains("Children")){
            nbtCompound.putBoolean("structureRestrictionActive", structureStart.hasActiveRestrictions());
            if(structureStart.getStructureRestrictionInstance() != null){
                nbtCompound.putInt("structureRestrictionInstancePower", structureStart.getStructureRestrictionInstance().getCurrentSharedPower());
                Map<AbstractStructureRestriction, Integer> map = structureStart.getStructureRestrictionInstance().getCurrentSeperatePowers();
                if(!map.isEmpty()){
                    map.forEach((res, power) -> {
                        nbtCompound.putInt("structureRestrictionInstancePower" + res.getRestrictionType(), power);
                    });
                }
            }
        }
    }

}
