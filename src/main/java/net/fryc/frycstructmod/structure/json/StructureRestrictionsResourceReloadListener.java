package net.fryc.frycstructmod.structure.json;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fryc.frycstructmod.FrycStructMod;
import net.fryc.frycstructmod.structure.RegisteredRestrictions;
import net.fryc.frycstructmod.structure.StructureRestriction;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.io.InputStream;

public class StructureRestrictionsResourceReloadListener implements SimpleSynchronousResourceReloadListener {

    private static final String STRUCTURE_RESTRICTIONS_PATH = "structure_restrictions";

    @Override
    public Identifier getFabricId() {
        return new Identifier(FrycStructMod.MOD_ID, STRUCTURE_RESTRICTIONS_PATH);
    }

    @Override
    public void reload(ResourceManager manager) {
        for(Identifier id : manager.findResources(STRUCTURE_RESTRICTIONS_PATH, path -> path.getPath().endsWith(".json")).keySet()) {
            try(InputStream stream = manager.getResource(id).get().getInputStream()) {
                JsonObject jsonObject = JsonParser.parseString(new String(stream.readAllBytes())).getAsJsonObject();

                String identifier = JsonHelper.getString(jsonObject, "structure_id");
                JsonObject miningObject = JsonHelper.getObject(jsonObject, "mining");
                JsonObject placingObject = JsonHelper.getObject(jsonObject, "placing");
                JsonArray mineExcept = JsonHelper.getArray(miningObject, "except", new JsonArray());
                JsonArray placeExcept = JsonHelper.getArray(placingObject, "except", new JsonArray());

                boolean allowMining = JsonHelper.getBoolean(miningObject, "allow", true);
                boolean allowMiningPlayerBlocks = JsonHelper.getBoolean(miningObject, "always_allow_when_placed_by_player", true);
                ImmutableSet<Block> miningExceptions = getExcludedBlocks(mineExcept, id);

                boolean allowPlacing = JsonHelper.getBoolean(placingObject, "allow", true);
                ImmutableSet<Block> placingExceptions = getExcludedBlocks(placeExcept, id);

                RegisteredRestrictions.STRUCTURE_RESTRICTIONS.put(identifier, new StructureRestriction(
                        identifier, allowMining, allowMiningPlayerBlocks, miningExceptions, allowPlacing, placingExceptions
                ));



            } catch(Exception e) {
                FrycStructMod.LOGGER.error("Error occurred while loading resource json" + id.toString(), e);
            }
        }
    }


    private static ImmutableSet<Block> getExcludedBlocks(JsonArray jsonArray, Identifier fileId){

        // TODO dodac obsluge tagow (musialbym jakos przechowywac nazwy tagow a potem dopiero z tych nazw brac tagi)
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
}
