package net.fryc.frycstructmod.util;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fryc.frycstructmod.FrycStructMod;
import net.fryc.frycstructmod.network.ModPackets;
import net.fryc.frycstructmod.structure.restrictions.AbstractStructureRestriction;
import net.fryc.frycstructmod.structure.restrictions.StructureRestrictionInstance;
import net.fryc.frycstructmod.structure.restrictions.sources.PersistentMobSourceEntry;
import net.fryc.frycstructmod.structure.restrictions.sources.SourceEntry;
import net.fryc.frycstructmod.util.interfaces.CanBeAffectedByStructure;
import net.fryc.frycstructmod.util.interfaces.ControlsStructureTick;
import net.fryc.frycstructmod.util.interfaces.HasRestrictions;
import net.fryc.frycstructmod.util.interfaces.HoldsStructureStart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructureStart;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.structure.Structure;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class ServerRestrictionsHelper {

    public static final int STRUCTURE_TICK_DELAY = 30;
    public static final int PLAYER_JOIN_STRUCTURE_TICK_DELAY = 120;

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
                        if(RestrictionsHelper.findPersistentMob(world, start.getBoundingBox(), type.get(), ((PersistentMobSourceEntry) entry).shouldForcePersistent())){
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

    // TODO zrobic eventy zeby odpalalo tutaj w opdowiednim miejscu i wtedy by sie ten persistent robil
    // TODO zrobic zeby to swiat tickowal strukture a nie gracz (bo jak wielu graczy bedzie w strukturze to bedzie wiele razy tickowana, powinno sprawdzac czy powinna sie tickowac i wtedy tickowac)
    public static void tickStructure(PlayerEntity pl, Structure structure){
        ServerPlayerEntity player = ((ServerPlayerEntity) pl);
        ServerWorld world = player.getServerWorld();
        StructureStart start = world.getStructureAccessor().getStructureAt(player.getBlockPos(), structure);
        HasRestrictions startWithRestrictions = ((HasRestrictions) (Object) start);

        if(!((ControlsStructureTick) world).canTickStructure(start)){
            return;
        }

        if(startWithRestrictions.hasActiveRestrictions()){
            if(startWithRestrictions.getStructureRestrictionInstance() == null){
                if(!startWithRestrictions.createStructureRestrictionInstance(world.getRegistryManager())){
                    return;
                }
            }

            Identifier id = world.getRegistryManager().get(RegistryKeys.STRUCTURE).getId(structure);
            if(id != null){
                ServerRestrictionsHelper.onStructureTick(player, start, startWithRestrictions.getStructureRestrictionInstance(), id);
            }
            else {
                FrycStructMod.LOGGER.error("Failed to get identifier of the following structure type: " + structure.getType().getClass().getName());
            }
        }
        else {
            ServerRestrictionsHelper.resetCurrentStructureWhenNeeded(player);
        }
    }

    public static void onStructureTick(ServerPlayerEntity player, StructureStart start, StructureRestrictionInstance restrictionInstance, Identifier structureId){
        if(start != ((HoldsStructureStart)player).getStructureStart()) {
            ServerRestrictionsHelper.onStructureEnter(player, start, restrictionInstance, structureId);
        }

        List<Entity> entities = player.getWorld().getOtherEntities(player, Box.from(start.getBoundingBox()), entity -> {
            return entity instanceof LivingEntity living && living.isAlive() &&
                    !living.isPlayer() && !((CanBeAffectedByStructure)living).isAffectedByStructure();
        });

        entities.forEach(entity -> {
            ((CanBeAffectedByStructure) entity).setAffectedByStructure(structureId.toString());
        });
    }

    public static void onStructureEnter(ServerPlayerEntity player, StructureStart start, StructureRestrictionInstance restrictionInstance, Identifier structureId){
        if(!ServerRestrictionsHelper.tryToRemoveRestrictionsFromStructure(start, restrictionInstance)){
            ((HoldsStructureStart) player).setStructureStart(start);
            ServerRestrictionsHelper.setAffectedByStructureServerAndClient(player, structureId.toString(), restrictionInstance);
            player.sendMessage(Text.of("Weszlem do struktury"));// TODO jakies FAJNE powiadomienie ze jestes na terenie struktury

            // checks for persistent entities on enter in case they somehow died (without player's help)
            ServerRestrictionsHelper.checkForPersistentEntitiesFromSource(restrictionInstance, player.getServerWorld(), start);

            // TODO odpalic eventy

        }
    }

    public static void resetCurrentStructureWhenNeeded(PlayerEntity player){
        HoldsStructureStart structureGetter = ((HoldsStructureStart) player);
        if(structureGetter.getStructureStart() != null){
            player.getWorld().getChunk(structureGetter.getStructureStart().getPos().x, structureGetter.getStructureStart().getPos().z).setNeedsSaving(true);
            player.sendMessage(Text.of("Wychodze"));
            structureGetter.setStructureStart(null);
            ServerRestrictionsHelper.setAffectedByStructureServerAndClient(player, "", null);
        }
    }

    // TODO
    /*
    void doEventa(){

        RestrictionsHelper.getRestrictionByType("status_effect", id).ifPresent(restriction -> {
            restriction.executeWhenEnabled(player.getServerWorld(), player.getBlockPos(), structure, (structureStart, instance) -> {
                if(restriction instanceof StatusEffectStructureRestriction statusRes){
                    statusRes.getPersistentEffects().forEach((effect, triplet) -> {
                        // TODO dac tu te persistent effecty ale najpierw posprzatac bo taki balagan jest w kodzie ze sie gubie
                    });
                }
            });
        });
    }

     */

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
