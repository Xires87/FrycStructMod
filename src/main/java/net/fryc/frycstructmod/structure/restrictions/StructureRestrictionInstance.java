package net.fryc.frycstructmod.structure.restrictions;

public class StructureRestrictionInstance {

    // TODO zrobic to jako liste zeby wiele restrykcji moglo byc aktywnych
    private final AbstractStructureRestriction structureRestriction;
    private int currentPower;

    public StructureRestrictionInstance(AbstractStructureRestriction structureRestriction){
        this.structureRestriction = structureRestriction;
        this.currentPower = structureRestriction.getRestrictionSource().getPower();
    }


    public AbstractStructureRestriction getStructureRestriction(){
        return this.structureRestriction;
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
}
