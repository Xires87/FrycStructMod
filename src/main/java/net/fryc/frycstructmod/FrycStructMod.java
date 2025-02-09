package net.fryc.frycstructmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fryc.frycstructmod.structure.restrictions.registry.FrycStructRestrictions;
import net.fryc.frycstructmod.structure.restrictions.registry.RestrictionRegistries;
import net.fryc.frycstructmod.structure.restrictions.json.StructureRestrictionsResourceReloadListener;
import net.minecraft.resource.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrycStructMod implements ModInitializer {

	public static final String MOD_ID = "frycstructmod";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		FrycStructRestrictions.registerSourceEntryTypes();
		FrycStructRestrictions.registerRestrictionTypes();


		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new StructureRestrictionsResourceReloadListener());
	}
}