package net.fryc.frycstructmod;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fryc.frycstructmod.structure.restrictions.RestrictionRegistries;
import net.fryc.frycstructmod.structure.restrictions.json.StructureRestrictionsResourceReloadListener;
import net.fryc.frycstructmod.structure.restrictions.sources.BlockStateSourceEntry;
import net.fryc.frycstructmod.structure.restrictions.sources.SourceEntry;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
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
		RestrictionRegistries.registerSourceEntryTypes();
		RestrictionRegistries.registerRestrictionTypes();


		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new StructureRestrictionsResourceReloadListener());
	}
}