package net.fryc.frycstructmod.structure.restrictions.registry;

import com.google.gson.JsonObject;
import net.fryc.frycstructmod.structure.restrictions.AbstractStructureRestriction;
import net.fryc.frycstructmod.structure.restrictions.sources.SourceEntry;
import net.minecraft.util.Identifier;

import java.util.HashMap;

public class RestrictionRegistries {

    // TODO zabezpieczyc te mapy (dac ochrone przed podwojna rejestracja itd)

    public static final HashMap<String, AbstractStructureRestriction> STRUCTURE_RESTRICTIONS = new HashMap<>();

    public static final HashMap<String, RestrictionTypeLoader> RESTRICTION_TYPES = new HashMap<>();

    public static final HashMap<String, SourceEntryLoader> SOURCE_ENTRY_TYPES = new HashMap<>();





    @FunctionalInterface
    public interface SourceEntryLoader {
        SourceEntry<?> loadFromJson(JsonObject jsonObject, Identifier id);
    }


    @FunctionalInterface
    public interface RestrictionTypeLoader {
        void loadFromJson(JsonObject jsonObject, Identifier id);
    }
}
