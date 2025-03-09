package net.fryc.frycstructmod.structure.restrictions;

import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.Set;

public class StructureRestrictionInstance {
// TODO zrobic zeby sourcey byly shared albo separate i jesli shared to trzeba okreslic czy nalezy dodawac te wartosci czy jak
    private final Set<AbstractStructureRestriction> structureRestrictions;
    private int currentPower;

    public StructureRestrictionInstance(Collection<AbstractStructureRestriction> structureRestrictions){
        this(convertCollectionToSet(structureRestrictions));
    }

    public StructureRestrictionInstance(Set<AbstractStructureRestriction> structureRestrictions){
        this.structureRestrictions = structureRestrictions;
        this.currentPower = calculatePower(structureRestrictions);
    }


    public Set<AbstractStructureRestriction> getStructureRestriction(){
        return this.structureRestrictions;
    }

    public int getCurrentPower(){
        return this.currentPower;
    }

    public void setCurrentPower(int power){
        this.currentPower = power;
    }

    /**
     *  Returns true when currentPower is lower than 1 after subtraction
     */
    public boolean decreaseCurrentPower(int power){
        this.currentPower -= power;

        return this.currentPower < 1;
    }

    private static int calculatePower(Set<AbstractStructureRestriction> structureRestrictions){
        int i = 0;
        for(AbstractStructureRestriction restriction : structureRestrictions){
            i += restriction.getRestrictionSource().getPower();
        }

        return i;
    }

    private static Set<AbstractStructureRestriction> convertCollectionToSet(Collection<AbstractStructureRestriction> collection){
        ImmutableSet.Builder<AbstractStructureRestriction> builder = ImmutableSet.builder();
        collection.forEach(builder::add);
        return builder.build();
    }
}
