package net.fryc.frycstructmod.mixin;


import net.fryc.frycstructmod.util.interfaces.HasRestrictions;
import net.minecraft.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StructureStart.class)
abstract class StructureStartMixin implements HasRestrictions {

    private boolean restrictionsActive = true;

    public boolean hasActiveRestrictions(){
        return this.restrictionsActive;
    }

    public void setRestrictions(boolean active){
        this.restrictionsActive = active;
    }
}
