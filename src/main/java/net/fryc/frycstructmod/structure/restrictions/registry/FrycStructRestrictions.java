package net.fryc.frycstructmod.structure.restrictions.registry;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fryc.frycstructmod.structure.restrictions.DefaultStructureRestriction;
import net.fryc.frycstructmod.structure.restrictions.StatusEffectStructureRestriction;
import net.fryc.frycstructmod.structure.restrictions.sources.BlockStateSourceEntry;
import net.fryc.frycstructmod.structure.restrictions.sources.ItemStackSourceEntry;
import net.fryc.frycstructmod.structure.restrictions.sources.LivingEntitySourceEntry;
import net.fryc.frycstructmod.structure.restrictions.sources.PersistentMobSourceEntry;
import net.fryc.frycstructmod.util.FrycJsonHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import oshi.util.tuples.Quartet;
import oshi.util.tuples.Triplet;

import java.util.Map;

public class FrycStructRestrictions {

    public static void registerRestrictionTypes(){
        RestrictionRegistries.registerRestrictionType("default", ((jsonObject, id) -> {
            String identifier = JsonHelper.getString(jsonObject, "structure_id");
            JsonObject miningObject = JsonHelper.getObject(jsonObject, "mining");
            JsonObject placingObject = JsonHelper.getObject(jsonObject, "placing");
            JsonArray mineExcept = JsonHelper.getArray(miningObject, "except", new JsonArray());
            JsonArray placeExcept = JsonHelper.getArray(placingObject, "except", new JsonArray());

            boolean allowMining = JsonHelper.getBoolean(miningObject, "allow", true);
            boolean allowMiningPlayerBlocks = JsonHelper.getBoolean(miningObject, "always_allow_when_placed_by_player", true);
            float miningSpeedMultiplier = JsonHelper.getFloat(miningObject, "mining_speed_multiplier", 0.0027F);
            ImmutableSet<Block> miningExceptions = FrycJsonHelper.getExcludedBlocks(mineExcept, id);

            boolean allowPlacing = JsonHelper.getBoolean(placingObject, "allow", true);
            boolean disallowPlacingIndestructibleBlocks = JsonHelper.getBoolean(miningObject, "always_disallow_when_indestructible", true);
            ImmutableSet<Block> placingExceptions = FrycJsonHelper.getExcludedBlocks(placeExcept, id);

            RestrictionRegistries.registerStructureRestriction(identifier, "default", new DefaultStructureRestriction(
                    identifier, allowMining, allowMiningPlayerBlocks, miningSpeedMultiplier, miningExceptions, allowPlacing,
                    disallowPlacingIndestructibleBlocks, placingExceptions, FrycJsonHelper.createRestrictionSource(jsonObject, id)
            ));
        }));

        RestrictionRegistries.registerRestrictionType("status_effect", ((jsonObject, id) -> {
            String identifier = JsonHelper.getString(jsonObject, "structure_id");
            JsonObject entitiesObject = JsonHelper.getObject(jsonObject, "entities_affected");
            JsonObject allowedEffectsObject = JsonHelper.getObject(jsonObject, "allowed_effects");

            JsonArray entityExcept = JsonHelper.getArray(entitiesObject, "except", new JsonArray());
            boolean affectAll = JsonHelper.getBoolean(entitiesObject, "affect_all", false);
            ImmutableSet<EntityType<?>> entityExceptions = FrycJsonHelper.getExcludedEntities(entityExcept, id);

            JsonArray allowedStatusExcept = JsonHelper.getArray(allowedEffectsObject, "except", new JsonArray());
            boolean allowEffects = JsonHelper.getBoolean(allowedEffectsObject, "allowed", true);
            ImmutableSet<StatusEffect> allowedEffectExceptions = FrycJsonHelper.getExcludedStatusEffects(allowedStatusExcept, id);

            JsonArray persistentEffectsArray = JsonHelper.getArray(jsonObject, "structure_effects");
            Map<StatusEffect, Triplet<Quartet<Boolean, Boolean, Boolean, Boolean>, Integer, Integer>> persistentEffectsMap = FrycJsonHelper.getPersistentEffectsMap(persistentEffectsArray, id);

            RestrictionRegistries.registerStructureRestriction(identifier, "status_effect", new StatusEffectStructureRestriction(
                    identifier, affectAll, entityExceptions, allowEffects, allowedEffectExceptions,
                    persistentEffectsMap, FrycJsonHelper.createRestrictionSource(jsonObject, id)
            ));
        }));
    }


    public static void registerSourceEntryTypes(){
        RestrictionRegistries.registerSourceEntryType("blockBreak", (jsonObject, id) -> {
            String stringId = JsonHelper.getString(jsonObject, "entry_id");
            int sourceStrength = JsonHelper.getInt(jsonObject, "strength");
            Identifier blockId = Registries.BLOCK.getIds().stream().filter(identifier -> {
                return identifier.toString().equals(stringId);
            }).findFirst().get();

            return new BlockStateSourceEntry(blockId, sourceStrength);
        });

        RestrictionRegistries.registerSourceEntryType("mobKill", (jsonObject, id) -> {
            String stringId = JsonHelper.getString(jsonObject, "entry_id");
            int sourceStrength = JsonHelper.getInt(jsonObject, "strength");
            Identifier mobId = Registries.ENTITY_TYPE.getIds().stream().filter(identifier -> {
                return identifier.toString().equals(stringId);
            }).findFirst().get();

            return new LivingEntitySourceEntry(mobId, sourceStrength);
        });

        RestrictionRegistries.registerSourceEntryType("persistentMobKill", (jsonObject, id) -> {
            String stringId = JsonHelper.getString(jsonObject, "entry_id");
            int sourceStrength = JsonHelper.getInt(jsonObject, "strength");
            Identifier mobId = Registries.ENTITY_TYPE.getIds().stream().filter(identifier -> {
                return identifier.toString().equals(stringId);
            }).findFirst().get();

            boolean checkForOther = JsonHelper.getBoolean(jsonObject, "checkForOtherPersistentMobs", true);
            boolean forcePersistent = JsonHelper.getBoolean(jsonObject, "forcePersistent", true);

            return new PersistentMobSourceEntry(mobId, sourceStrength, forcePersistent, checkForOther);
        });

        RestrictionRegistries.registerSourceEntryType("itemUseFinish", (jsonObject, id) -> {
            String stringId = JsonHelper.getString(jsonObject, "entry_id");
            int sourceStrength = JsonHelper.getInt(jsonObject, "strength");
            Identifier itemId = Registries.ITEM.getIds().stream().filter(identifier -> {
                return identifier.toString().equals(stringId);
            }).findFirst().get();

            return new ItemStackSourceEntry(itemId, sourceStrength);
        });
    }
}
