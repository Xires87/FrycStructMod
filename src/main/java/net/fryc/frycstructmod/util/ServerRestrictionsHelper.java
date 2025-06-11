package net.fryc.frycstructmod.util;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fryc.frycstructmod.FrycStructMod;
import net.fryc.frycstructmod.network.ModPackets;
import net.fryc.frycstructmod.structure.restrictions.AbstractStructureRestriction;
import net.fryc.frycstructmod.structure.restrictions.StatusEffectStructureRestriction;
import net.fryc.frycstructmod.structure.restrictions.StructureRestrictionInstance;
import net.fryc.frycstructmod.structure.restrictions.registry.RestrictionTypes;
import net.fryc.frycstructmod.structure.restrictions.sources.AbstractSourceEntry;
import net.fryc.frycstructmod.structure.restrictions.sources.PersistentMobSourceEntry;
import net.fryc.frycstructmod.structure.restrictions.sources.RestrictionSource;
import net.fryc.frycstructmod.structure.restrictions.sources.SourceEntry;
import net.fryc.frycstructmod.structure.restrictions.sources.events.SourceEntryEvent;
import net.fryc.frycstructmod.util.interfaces.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
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
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Quartet;

import java.util.ArrayList;
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

    public static void sendRestrictionUpdatesToClient(StructureRestrictionInstance restrictionInstance, StructureStart start, ServerWorld world, String structureId){
        ((PlayerLocator)(Object) start).getPlayersInStructure(world).forEach(pl -> {
            ServerRestrictionsHelper.setAffectedByStructureServerAndClient(pl, structureId, restrictionInstance);
        });
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

    public static void checkForPersistentEntitiesFromAllSourcesAndUpdate(MobEntity mob, StructureRestrictionInstance restrictionInstance, ServerWorld world, StructureStart start){
        if(restrictionInstance != null){
            restrictionInstance.getActiveRestrictions().forEach(restriction -> {
                restriction.getRestrictionSource().getEntries().stream().filter(entry -> {
                    return entry.getEvent().equals(SourceEntryEvent.ON_MOB_KILL) &&
                            entry instanceof PersistentMobSourceEntry &&
                            Registries.ENTITY_TYPE.getId(mob.getType()).equals(((AbstractSourceEntry<?>) entry).getSourceId());
                }).forEach(entry -> {
                    PersistentMobSourceEntry mobEntry = (PersistentMobSourceEntry) entry;
                    if(PersistentMobSourceEntry.isOwnerShared(mobEntry)){
                        mobEntry.checkAndUpdateSharedRestrictions(restrictionInstance, start, world, mob.getType());
                    }
                    else {
                        mobEntry.checkAndUpdateSeparateRestrictions(restrictionInstance, start, world, mob.getType());
                    }
                });
            });
        }
    }

    public static void checkForPersistentEntitiesFromAllSourcesAndUpdate(StructureRestrictionInstance restrictionInstance, ServerWorld world, StructureStart start){
        if(restrictionInstance != null){
            restrictionInstance.getActiveRestrictions().forEach(restriction -> {
                restriction.getRestrictionSource().getEntries().stream().filter(entry -> {
                    return entry.getEvent().equals(SourceEntryEvent.ON_MOB_KILL) && entry instanceof PersistentMobSourceEntry;
                }).forEach(entry -> {
                    PersistentMobSourceEntry mobEntry = (PersistentMobSourceEntry) entry;
                    if(PersistentMobSourceEntry.isOwnerShared(mobEntry)){
                        mobEntry.checkAndUpdateSharedRestrictions(restrictionInstance, start, world, Registries.ENTITY_TYPE.get(mobEntry.getSourceId()));
                    }
                    else {
                        mobEntry.checkAndUpdateSeparateRestrictions(restrictionInstance, start, world, Registries.ENTITY_TYPE.get(mobEntry.getSourceId()));
                    }
                });
            });
        }
    }

    public static void checkForPersistentEntitiesFromSeparateSourceAndUpdate(RestrictionSource source, StructureRestrictionInstance restrictionInstance, ServerWorld world, StructureStart start){
        if(restrictionInstance != null){
            List<SourceEntry<?>> list = source.getEntries().stream().filter(entry -> {
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
                restrictionInstance.getActiveRestrictions().stream().filter(restriction -> {
                    return restriction.getRestrictionSource().equals(source);
                }).forEach(restrictionInstance::disableRestriction);

                sendRestrictionUpdatesToClient(restrictionInstance, start, world, restrictionInstance.getStructureId());
            }

            tryToRemoveRestrictionsFromStructure(start, restrictionInstance);
        }
    }

    public static void checkForPersistentEntitiesFromSharedSourceAndUpdate(StructureRestrictionInstance restrictionInstance, ServerWorld world, StructureStart start){
        if(restrictionInstance != null){
            List<AbstractStructureRestriction> activeShared = restrictionInstance.getActiveRestrictions().stream().filter(restriction -> {
                return restriction.getRestrictionSource().isShared();
            }).toList();

            boolean shouldDisable = !activeShared.isEmpty();
            LABEL:
            for(AbstractStructureRestriction restriction : activeShared){
                List<SourceEntry<?>> list = restriction.getRestrictionSource().getEntries().stream().filter(entry -> {
                    return entry instanceof PersistentMobSourceEntry;
                }).toList();

                for(SourceEntry<?> entry : list){
                    Optional<EntityType<?>> type = EntityType.get(((PersistentMobSourceEntry) entry).getSourceId().toString());
                    if(type.isPresent()){
                        if(RestrictionsHelper.findPersistentMob(world, start.getBoundingBox(), type.get(), ((PersistentMobSourceEntry) entry).shouldForcePersistent())){
                            shouldDisable = false;
                            break LABEL;
                        }
                    }
                }
            }

            if(shouldDisable){
                // disabling one shared restriction disables others, so there is no need to iterate
                activeShared.stream().findFirst().ifPresent(restrictionInstance::disableRestriction);
                sendRestrictionUpdatesToClient(restrictionInstance, start, world, restrictionInstance.getStructureId());
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
        List<Entity> entities = player.getWorld().getOtherEntities(player, Box.from(start.getBoundingBox()), entity -> {
            return entity instanceof LivingEntity living && living.isAlive() && !living.isPlayer();
        });

        if(start != ((HoldsStructureStart)player).getStructureStart()) {
            if(((HoldsStructureStart)player).getStructureStart() != null){
                // executed when player enters one structure while leaving another
                ServerRestrictionsHelper.onStructureLeave(player);
            }
            ServerRestrictionsHelper.onStructureEnter(player, start, restrictionInstance, structureId, entities);
        }

        entities.forEach(entity -> {
            ((CanBeAffectedByStructure) entity).setAffectedByStructure(structureId.toString());
        });
    }

    public static void onStructureEnter(ServerPlayerEntity player, StructureStart start, StructureRestrictionInstance restrictionInstance, Identifier structureId, List<Entity> nonPlayerLivingEntities){
        if(!ServerRestrictionsHelper.tryToRemoveRestrictionsFromStructure(start, restrictionInstance)){
            ((HoldsStructureStart) player).setStructureStart(start);
            ServerRestrictionsHelper.setAffectedByStructureServerAndClient(player, structureId.toString(), restrictionInstance);

            ServerRestrictionsHelper.setAndSendRestrictionMessages(player, restrictionInstance);

            // checks for persistent entities on enter in case they somehow died (without player's help)
            ServerRestrictionsHelper.checkForPersistentEntitiesFromAllSourcesAndUpdate(restrictionInstance, player.getServerWorld(), start);

            // TODO odpalic eventy zamiast tego
            RestrictionsHelper.getRestrictionByType(RestrictionTypes.STATUS_EFFECT, structureId).ifPresent(restriction -> {
                if(!((HasRestrictions)(Object) start).getStructureRestrictionInstance().isRestrictionDisabled(restriction)){
                    if(restriction instanceof StatusEffectStructureRestriction statusRes){
                        statusRes.getPersistentEffects().forEach((effect, triplet) -> {
                            int amp = triplet.getB() - 1 > -1 ? triplet.getB() - 1 : 0;
                            int dur = triplet.getC() > 0 ? triplet.getC() : -1;
                            Quartet<Boolean, Boolean, Boolean, Boolean> quartet = triplet.getA();
                            if(quartet.getA()){
                                StatusEffectInstance instance = new StatusEffectInstance(effect, dur, amp, quartet.getB(), quartet.getC(), quartet.getD());
                                ((StructureStatusEffect) instance).setFromStructure(true);
                                player.addStatusEffect(instance);
                            }
                            else {
                                nonPlayerLivingEntities.forEach(entity -> {
                                    StatusEffectInstance instance = new StatusEffectInstance(effect, dur, amp, quartet.getB(), quartet.getC(), quartet.getD());
                                    ((StructureStatusEffect) instance).setFromStructure(true);
                                    ((LivingEntity) entity).addStatusEffect(instance);
                                });
                            }
                        });
                    }
                }
            });
        }
    }

    public static void onStructureLeave(PlayerEntity player){
        List<StatusEffect> structureEffects = new ArrayList<>();
        player.getActiveStatusEffects().forEach((effect, instance) -> {
            if(((StructureStatusEffect) instance).isFromStructure()){
                structureEffects.add(effect);
            }
            else {
                tryToRemoveHiddenEffect(instance);
            }
        });

        structureEffects.forEach(player::removeStatusEffect);
    }

    private static void tryToRemoveHiddenEffect(@Nullable StatusEffectInstance instance){
        if(instance != null){
            StatusEffectInstance hiddenEffect = ((StructureStatusEffect) instance).getHiddenEffect();
            if(hiddenEffect != null){
                if(((StructureStatusEffect) hiddenEffect).isFromStructure()){
                    ((StructureStatusEffect) instance).setHiddenEffect(((StructureStatusEffect) hiddenEffect).getHiddenEffect());
                    tryToRemoveHiddenEffect(instance);
                }
                else {
                    tryToRemoveHiddenEffect(hiddenEffect);
                }
            }
        }
    }

    public static void resetCurrentStructureWhenNeeded(PlayerEntity player){
        HoldsStructureStart structureGetter = ((HoldsStructureStart) player);
        if(structureGetter.getStructureStart() != null){
            player.getWorld().getChunk(structureGetter.getStructureStart().getPos().x, structureGetter.getStructureStart().getPos().z).setNeedsSaving(true);

            String mess = ((CanBeAffectedByStructure) player).getLeaveMessage();
            if(!mess.isEmpty()){
                player.sendMessage(Text.of(mess), true);
            }

            structureGetter.setStructureStart(null);
            ServerRestrictionsHelper.setAffectedByStructureServerAndClient(player, "", null);
            ServerRestrictionsHelper.onStructureLeave(player);
        }
    }

    public static void setAndSendRestrictionMessages(PlayerEntity player, StructureRestrictionInstance resInstance){
        String mess = resInstance.getWelcomeMessage();
        if(!mess.isEmpty()){
            player.sendMessage(Text.of(mess), true);
        }
        ((CanBeAffectedByStructure) player).setLeaveMessage(resInstance.getLeaveMessage());
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
