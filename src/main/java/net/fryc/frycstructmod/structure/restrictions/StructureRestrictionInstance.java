package net.fryc.frycstructmod.structure.restrictions;

import com.google.common.collect.ImmutableSet;
import net.fryc.frycstructmod.FrycStructMod;
import net.fryc.frycstructmod.structure.restrictions.sources.SourceEntry;
import net.fryc.frycstructmod.util.RestrictionsHelper;

import java.util.*;

public class StructureRestrictionInstance {
// TODO jesli shared to trzeba okreslic czy nalezy dodawac te wartosci czy jak
    private final Set<AbstractStructureRestriction> structureRestrictions;
    private int currentSharedPower;
    private final String structureId;

    private final Map<AbstractStructureRestriction, Integer> currentSeperatePowers;

    private final Set<AbstractStructureRestriction> disabledRestrictions;

    public StructureRestrictionInstance(Collection<AbstractStructureRestriction> structureRestrictions){
        this(convertCollectionToSet(structureRestrictions));
    }

    public StructureRestrictionInstance(Set<AbstractStructureRestriction> structureRestrictions){
        if(structureRestrictions.isEmpty()){
            throw new IllegalArgumentException("Cannot create StructureRestrictionInstance with empty collection!");
        }

        this.structureRestrictions = structureRestrictions;
        this.currentSharedPower = calculateSharedPower(structureRestrictions);
        this.currentSeperatePowers = createSeparatePowers(structureRestrictions);
        this.disabledRestrictions = new HashSet<>();
        this.structureId = structureRestrictions.stream().findAny().get().getStructureId();
    }

    public boolean isActive(){
        return this.getDisabledRestrictions().size() < this.getStructureRestrictions().size();
    }

    public boolean isRestrictionDisabled(AbstractStructureRestriction restriction){
        return this.getDisabledRestrictions().contains(restriction);
    }

    public void updateDisabledRestrictions(){
        this.getStructureRestrictions().stream().filter(this::shouldDisableRestriction).forEach(restriction -> {
            this.getDisabledRestrictions().add(restriction);
        });
    }

    /**
     *  Disabling shared restriction disables all other shared restrictions
     */
    public void disableRestriction(AbstractStructureRestriction restriction){
        if(restriction.getRestrictionSource().isShared()){
            this.setCurrentSharedPower(0);
            this.updateDisabledRestrictions();
        }
        else {
            this.getCurrentSeperatePowers().put(restriction, 0);
            this.getDisabledRestrictions().add(restriction);
        }
    }

    private boolean shouldDisableRestriction(AbstractStructureRestriction restriction){
        if(restriction.getRestrictionSource().isShared()){
            return this.getCurrentSharedPower() < 1;
        }

        return this.getCurrentSeperatePowers().get(restriction) == null || this.getCurrentSeperatePowers().get(restriction) < 1;
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

    public Set<AbstractStructureRestriction> getActiveRestrictions(){
        Set<AbstractStructureRestriction> set = new HashSet<>();
        this.getStructureRestrictions().stream().filter(res -> {
            return !this.isRestrictionDisabled(res);
        }).forEach(set::add);

        return set;
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

    public String getStructureId(){
        return this.structureId;
    }

    public Map<AbstractStructureRestriction, Integer> getCurrentSeperatePowers() {
        return currentSeperatePowers;
    }

    public Set<AbstractStructureRestriction> getDisabledRestrictions(){
        return this.disabledRestrictions;
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
