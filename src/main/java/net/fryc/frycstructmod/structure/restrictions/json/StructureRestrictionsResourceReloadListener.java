package net.fryc.frycstructmod.structure.restrictions.json;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fryc.frycstructmod.FrycStructMod;
import net.fryc.frycstructmod.structure.restrictions.registry.RestrictionRegistries;
import net.fryc.frycstructmod.util.FrycJsonHelper;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.util.HashMap;

public class StructureRestrictionsResourceReloadListener implements SimpleSynchronousResourceReloadListener {

    private static final String STRUCTURE_RESTRICTIONS_PATH = "structure_restrictions";
    public static final HashMap<Identifier, String> SAVED_JSONS = new HashMap<>();

    @Override
    public Identifier getFabricId() {
        return new Identifier(FrycStructMod.MOD_ID, STRUCTURE_RESTRICTIONS_PATH);
    }

    @Override
    public void reload(ResourceManager manager) {
        RestrictionRegistries.STRUCTURE_RESTRICTIONS.clear();
        SAVED_JSONS.clear();

        for(Identifier id : manager.findResources(STRUCTURE_RESTRICTIONS_PATH, path -> path.getPath().endsWith(".json")).keySet()) {
            try(InputStream stream = manager.getResource(id).get().getInputStream()) {
                String jsonString = new String(stream.readAllBytes());
                FrycJsonHelper.loadStructureRestriction(id, jsonString);
                SAVED_JSONS.put(id, jsonString);
            } catch(Exception e) {
                FrycStructMod.LOGGER.error("Error occurred while loading resource json" + id.toString(), e);
            }
        }
    }

}
