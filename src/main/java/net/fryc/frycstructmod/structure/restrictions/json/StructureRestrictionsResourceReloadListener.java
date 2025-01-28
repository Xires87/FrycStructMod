package net.fryc.frycstructmod.structure.restrictions.json;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fryc.frycstructmod.FrycStructMod;
import net.fryc.frycstructmod.structure.restrictions.RestrictionRegistries;
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
                String restrictionType = JsonHelper.getString(jsonObject, "type");
                RestrictionRegistries.RESTRICTION_TYPES.get(restrictionType).loadFromJson(jsonObject, id);

            } catch(Exception e) {
                FrycStructMod.LOGGER.error("Error occurred while loading resource json" + id.toString(), e);
            }
        }
    }

}
