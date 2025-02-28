package net.fryc.frycstructmod.structure.restrictions.registry;

import com.google.gson.JsonObject;
import net.fryc.frycstructmod.FrycStructMod;
import net.fryc.frycstructmod.structure.restrictions.AbstractStructureRestriction;
import net.fryc.frycstructmod.structure.restrictions.sources.SourceEntry;
import net.minecraft.util.Identifier;

import java.util.HashMap;

public class RestrictionRegistries {


    public static final HashMap<String, HashMap<String, AbstractStructureRestriction>> STRUCTURE_RESTRICTIONS = new HashMap<>();

    public static final HashMap<String, RestrictionTypeLoader> RESTRICTION_TYPES = new HashMap<>();

    public static final HashMap<String, SourceEntryLoader> SOURCE_ENTRY_TYPES = new HashMap<>();


    public static void registerSourceEntryType(String sourceEntryType, SourceEntryLoader jsonLoader){
        if(SOURCE_ENTRY_TYPES.containsKey(sourceEntryType)){
            String msg = "Source entry type with the following name: '" + sourceEntryType +
                    "' is already registered!";
            FrycStructMod.LOGGER.error(msg);
        }
        else {
            SOURCE_ENTRY_TYPES.put(sourceEntryType, jsonLoader);
        }
    }

    public static void registerRestrictionType(String restrictionType, RestrictionTypeLoader jsonLoader){
        if(RESTRICTION_TYPES.containsKey(restrictionType)){
            String msg = "Restriction type with the following name: '" + restrictionType +
                    "' is already registered!";
            FrycStructMod.LOGGER.error(msg);
        }
        else {
            RESTRICTION_TYPES.put(restrictionType, jsonLoader);
        }
    }

    public static void registerStructureRestriction(String structureId, String restrictionType, AbstractStructureRestriction restriction){
        if(STRUCTURE_RESTRICTIONS.containsKey(structureId)){
            if(STRUCTURE_RESTRICTIONS.get(structureId).containsKey(restrictionType)){
                String msg = "The following structure: '" + structureId +
                        "' already has a restriction of the following type: '" + restrictionType +
                        "'! One structure cannot have multiple restrictions of the same type!";
                FrycStructMod.LOGGER.error(msg);
            }
            else {
                STRUCTURE_RESTRICTIONS.get(structureId).put(restrictionType, restriction);
            }
        }
        else {
            HashMap<String, AbstractStructureRestriction> map = new HashMap<>();
            map.put(restrictionType, restriction);
            STRUCTURE_RESTRICTIONS.put(structureId, map);
        }
    }



    @FunctionalInterface
    public interface SourceEntryLoader {
        SourceEntry<?> loadFromJson(JsonObject jsonObject, Identifier id);
    }


    @FunctionalInterface
    public interface RestrictionTypeLoader {
        void loadFromJson(JsonObject jsonObject, Identifier id);
    }
}
