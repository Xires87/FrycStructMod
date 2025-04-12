package net.fryc.frycstructmod.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
import net.fryc.frycstructmod.FrycStructMod;
import net.fryc.frycstructmod.structure.restrictions.registry.RestrictionRegistries;
import net.fryc.frycstructmod.structure.restrictions.sources.RestrictionSource;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import oshi.util.tuples.Quartet;
import oshi.util.tuples.Triplet;

public class FrycJsonHelper {

    public static void loadStructureRestriction(Identifier fileId, String jsonString){
        JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
        String restrictionType = JsonHelper.getString(jsonObject, "type", "default");
        RestrictionRegistries.RESTRICTION_TYPES.get(restrictionType).loadFromJson(jsonObject, fileId);
    }

    public static ImmutableSet<Block> getExcludedBlocks(JsonArray jsonArray, Identifier fileId){
        ImmutableSet.Builder<Block> builder = ImmutableSet.builder();
        for(JsonElement element : jsonArray){
            try{
                Item blockItem = JsonHelper.asItem(element, "blockFromJsonArray");
                if(blockItem instanceof BlockItem block){
                    builder.add(block.getBlock());
                }
            } catch (Exception e) {
                FrycStructMod.LOGGER.error("Error occurred while loading excluded blocks from the following file: " + fileId.toString(), e);
            }
        }

        return builder.build();
    }

    public static ImmutableSet<EntityType<?>> getExcludedEntities(JsonArray jsonArray, Identifier fileId){
        ImmutableSet.Builder<EntityType<?>> builder = ImmutableSet.builder();
        for(JsonElement element : jsonArray){
            try{
                EntityType<?> entity = asEntityType(element, "entityFromJsonArray");
                builder.add(entity);

            } catch (Exception e) {
                FrycStructMod.LOGGER.error("Error occurred while loading excluded entities from the following file: " + fileId.toString(), e);
            }
        }

        return builder.build();
    }

    public static ImmutableSet<StatusEffect> getExcludedStatusEffects(JsonArray jsonArray, Identifier fileId){
        ImmutableSet.Builder<StatusEffect> builder = ImmutableSet.builder();
        for(JsonElement element : jsonArray){
            try{
                StatusEffect statusEffect = asStatusEffect(element, "statusEffectFromJsonArray");
                builder.add(statusEffect);

            } catch (Exception e) {
                FrycStructMod.LOGGER.error("Error occurred while loading excluded status effects from the following file: " + fileId.toString(), e);
            }
        }

        return builder.build();
    }

    public static ImmutableMap<StatusEffect, Triplet<Quartet<Boolean, Boolean, Boolean, Boolean>, Integer, Integer>> getPersistentEffectsMap(JsonArray jsonArray, Identifier fileId){
        ImmutableMap.Builder<StatusEffect, Triplet<Quartet<Boolean, Boolean, Boolean, Boolean>, Integer, Integer>> builder = ImmutableMap.builder();
        for(JsonElement element : jsonArray){
            try{
                JsonObject statusEffectObject = JsonHelper.asObject(element, "StatusEffectObject");
                JsonElement effectElement = JsonHelper.getElement(statusEffectObject, "id");
                int amplifier = JsonHelper.getInt(statusEffectObject, "amplifier");
                int duration = JsonHelper.getInt(statusEffectObject, "duration");
                boolean forPlayer = JsonHelper.getBoolean(statusEffectObject, "forPlayer", true);
                boolean ambient = JsonHelper.getBoolean(statusEffectObject, "ambient", false);
                boolean showParticles = JsonHelper.getBoolean(statusEffectObject, "showParticles", true);
                boolean showIcon = JsonHelper.getBoolean(statusEffectObject, "showIcon", true);
                builder.put(asStatusEffect(effectElement, "statusEffectFromJsonObject"), new Triplet<>(new Quartet<>(forPlayer, ambient, showParticles, showIcon), amplifier, duration));

            } catch (Exception e) {
                FrycStructMod.LOGGER.error("Error occurred while loading persistent status effects from the following file: " + fileId.toString(), e);
            }
        }

        return builder.build();
    }

    public static RestrictionSource createRestrictionSource(JsonObject jsonObject, Identifier id){
        JsonObject sourceObject = JsonHelper.getObject(jsonObject, "source");
        int power = JsonHelper.getInt(sourceObject, "power");
        boolean shared = JsonHelper.getBoolean(sourceObject, "shared", false);

        RestrictionSource.Builder builder = RestrictionSource.builder().setPower(power).setShared(shared);

        JsonArray sourceEntries = JsonHelper.getArray(sourceObject, "entries");
        for(JsonElement element : sourceEntries){
            try{
                JsonObject sourceTypeObject = JsonHelper.asObject(element, "SourceTypeObject");
                builder.putSourceEntry(
                        RestrictionRegistries.SOURCE_ENTRY_TYPES.get(JsonHelper.getString(sourceTypeObject, "type")).loadFromJson(sourceTypeObject, id)
                );
            } catch (Exception e) {
                FrycStructMod.LOGGER.error("Error occurred while loading source entries from the following file: " + id.toString(), e);
            }
        }

        return builder.build();
    }

    public static EntityType<?> asEntityType(JsonElement element, String name){
        if (element.isJsonPrimitive()) {
            String string = element.getAsString();
            return (EntityType<?>) Registries.ENTITY_TYPE.getOrEmpty(new Identifier(string)).orElseThrow(() -> new JsonSyntaxException("Expected " + name + " to be an entity, was unknown string '" + string + "'"));
        } else {
            throw new JsonSyntaxException("Expected " + name + " to be an entity, was " + JsonHelper.getType(element));
        }
    }

    public static StatusEffect asStatusEffect(JsonElement element, String name){
        if (element.isJsonPrimitive()) {
            String string = element.getAsString();
            return (StatusEffect) Registries.STATUS_EFFECT.getOrEmpty(new Identifier(string)).orElseThrow(() -> new JsonSyntaxException("Expected " + name + " to be a status effect, was unknown string '" + string + "'"));
        } else {
            throw new JsonSyntaxException("Expected " + name + " to be a status effect, was " + JsonHelper.getType(element));
        }
    }
}
