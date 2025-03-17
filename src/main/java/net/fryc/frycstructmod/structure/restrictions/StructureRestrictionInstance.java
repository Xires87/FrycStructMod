package net.fryc.frycstructmod.structure.restrictions;

import com.google.common.collect.ImmutableSet;
import net.fryc.frycstructmod.FrycStructMod;
import net.fryc.frycstructmod.structure.restrictions.registry.RestrictionRegistries;
import net.fryc.frycstructmod.structure.restrictions.sources.SourceEntry;
import net.fryc.frycstructmod.util.RestrictionsHelper;

import java.util.*;

public class StructureRestrictionInstance {
// TODO zrobic zeby sourcey byly shared albo separate i jesli shared to trzeba okreslic czy nalezy dodawac te wartosci czy jak
    private final Set<AbstractStructureRestriction> structureRestrictions;
    private int currentSharedPower;

    private final Map<AbstractStructureRestriction, Integer> currentSeperatePowers;

    public StructureRestrictionInstance(Collection<AbstractStructureRestriction> structureRestrictions){
        this(convertCollectionToSet(structureRestrictions));
    }

    public StructureRestrictionInstance(Set<AbstractStructureRestriction> structureRestrictions){
        this.structureRestrictions = structureRestrictions;
        this.currentSharedPower = calculateSharedPower(structureRestrictions);
        this.currentSeperatePowers = createSeparatePowers(structureRestrictions);
    }


    public Set<AbstractStructureRestriction> getStructureRestrictions(){
        return this.structureRestrictions;
    }

    public int getCurrentSharedPower(){
        return this.currentSharedPower;
    }

    public void setCurrentSharedPower(int power){
        this.currentSharedPower = power;
    }

    public Map<AbstractStructureRestriction, Integer> getCurrentSeperatePowers() {
        return currentSeperatePowers;
    }

    public boolean isRestrictionDisabled(AbstractStructureRestriction restriction){
        if(restriction.getRestrictionSource().isShared()){
            return this.getCurrentSharedPower() < 1;
        }

        return this.getCurrentSeperatePowers().get(restriction) == null || this.getCurrentSeperatePowers().get(restriction) < 1;
    }

    public Set<String> getDisabledRestrictionsIds(){
        HashSet<String> set = new HashSet<>();
        this.getStructureRestrictions().stream().findAny().ifPresent(id -> {
            Set<Map.Entry<String, AbstractStructureRestriction>> entries = RestrictionRegistries.STRUCTURE_RESTRICTIONS.get(id.getStructureId()).entrySet();
            this.getStructureRestrictions().stream().filter(this::isRestrictionDisabled).forEach(restriction -> {
                entries.stream().filter(entry -> {
                    return entry.getValue().equals(restriction);
                }).forEach(entry -> set.add(entry.getKey()));
            });
        });

        return set;
    }

    /**
     *  Returns true when currentPower is lower than 1 after subtraction
     */
    public boolean decreaseCurrentPower(int power, SourceEntry<?> source){
        Optional<AbstractStructureRestriction> optional = RestrictionsHelper.getRestrictionBySource(this.getStructureRestrictions(), source);
        if(optional.isPresent()){
            AbstractStructureRestriction restriction = optional.get();
            if(restriction.getRestrictionSource().isShared()){
                this.currentSharedPower -= power;

                return this.currentSharedPower < 1;
            }
            else {
                int i = this.getCurrentSeperatePowers().get(restriction) - power;
                this.getCurrentSeperatePowers().put(restriction, i);

                return i < 1;
            }
        }
        else {
            FrycStructMod.LOGGER.error("Unable to decrease restriction's power, because restriction is null. This should never happen!");
            return false;
        }
    }

    private static int calculateSharedPower(Set<AbstractStructureRestriction> structureRestrictions){
        int i = 0;
        for(AbstractStructureRestriction restriction : structureRestrictions){
            if(restriction.getRestrictionSource().isShared()){
                i += restriction.getRestrictionSource().getPower();
            }
        }

        return i;
    }

    private static Map<AbstractStructureRestriction, Integer> createSeparatePowers(Set<AbstractStructureRestriction> structureRestrictions){
        HashMap<AbstractStructureRestriction, Integer> map = new HashMap<>();
        structureRestrictions.stream().filter(restriction -> !restriction.getRestrictionSource().isShared()).forEach(restriction -> {
            map.put(restriction, restriction.getRestrictionSource().getPower());
        });

        return map;
    }

    private static Set<AbstractStructureRestriction> convertCollectionToSet(Collection<AbstractStructureRestriction> collection){
        ImmutableSet.Builder<AbstractStructureRestriction> builder = ImmutableSet.builder();
        collection.forEach(builder::add);
        return builder.build();
    }


}
