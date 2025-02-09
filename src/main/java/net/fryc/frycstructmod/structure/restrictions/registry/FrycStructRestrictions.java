package net.fryc.frycstructmod.structure.restrictions.registry;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fryc.frycstructmod.FrycStructMod;
import net.fryc.frycstructmod.structure.restrictions.StructureRestriction;
import net.fryc.frycstructmod.structure.restrictions.sources.BlockStateSourceEntry;
import net.fryc.frycstructmod.structure.restrictions.sources.LivingEntitySourceEntry;
import net.fryc.frycstructmod.structure.restrictions.sources.PersistentMobSourceEntry;
import net.fryc.frycstructmod.structure.restrictions.sources.RestrictionSource;
import net.fryc.frycstructmod.util.FrycJsonHelper;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class FrycStructRestrictions {

    public static void registerRestrictionTypes(){
        RestrictionRegistries.RESTRICTION_TYPES.put("default", (jsonObject, id) -> {
            String identifier = JsonHelper.getString(jsonObject, "structure_id");
            JsonObject miningObject = JsonHelper.getObject(jsonObject, "mining");
            JsonObject placingObject = JsonHelper.getObject(jsonObject, "placing");
            JsonArray mineExcept = JsonHelper.getArray(miningObject, "except", new JsonArray());
            JsonArray placeExcept = JsonHelper.getArray(placingObject, "except", new JsonArray());

            boolean allowMining = JsonHelper.getBoolean(miningObject, "allow", true);
            boolean allowMiningPlayerBlocks = JsonHelper.getBoolean(miningObject, "always_allow_when_placed_by_player", true);
            ImmutableSet<Block> miningExceptions = FrycJsonHelper.getExcludedBlocks(mineExcept, id);

            boolean allowPlacing = JsonHelper.getBoolean(placingObject, "allow", true);
            ImmutableSet<Block> placingExceptions = FrycJsonHelper.getExcludedBlocks(placeExcept, id);

            JsonObject sourceObject = JsonHelper.getObject(jsonObject, "source");
            int power = JsonHelper.getInt(sourceObject, "power");

            RestrictionSource.Builder builder = RestrictionSource.builder().setPower(power);

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

            RestrictionRegistries.STRUCTURE_RESTRICTIONS.put(identifier, new StructureRestriction(
                    identifier, allowMining, allowMiningPlayerBlocks, miningExceptions, allowPlacing, placingExceptions, builder.build()
            ));
        });



    }


    public static void registerSourceEntryTypes(){
        RestrictionRegistries.SOURCE_ENTRY_TYPES.put("block", (jsonObject, id) -> {
            String stringId = JsonHelper.getString(jsonObject, "entry_id");
            int sourceStrength = JsonHelper.getInt(jsonObject, "strength");
            Identifier blockId = Registries.BLOCK.getIds().stream().filter(identifier -> {
                return identifier.toString().equals(stringId);
            }).findFirst().get();

            return new BlockStateSourceEntry(blockId, sourceStrength);
        });

        RestrictionRegistries.SOURCE_ENTRY_TYPES.put("mob", (jsonObject, id) -> {
            String stringId = JsonHelper.getString(jsonObject, "entry_id");
            int sourceStrength = JsonHelper.getInt(jsonObject, "strength");
            Identifier mobId = Registries.ENTITY_TYPE.getIds().stream().filter(identifier -> {
                return identifier.toString().equals(stringId);
            }).findFirst().get();

            return new LivingEntitySourceEntry(mobId, sourceStrength);
        });
// TODO zrobic zeby przy wejsciu sprawdzalo czy wszyscy persistent czasem nie zdechli
        RestrictionRegistries.SOURCE_ENTRY_TYPES.put("persistentMob", (jsonObject, id) -> {
            String stringId = JsonHelper.getString(jsonObject, "entry_id");
            int sourceStrength = JsonHelper.getInt(jsonObject, "strength");
            Identifier mobId = Registries.ENTITY_TYPE.getIds().stream().filter(identifier -> {
                return identifier.toString().equals(stringId);
            }).findFirst().get();

            return new PersistentMobSourceEntry(mobId, sourceStrength);
        });
    }
}
