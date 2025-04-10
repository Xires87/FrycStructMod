package net.fryc.frycstructmod.util.interfaces.client;

import net.fryc.frycstructmod.structure.restrictions.StructureRestrictionInstance;
import org.jetbrains.annotations.Nullable;

public interface HoldsStructureRestrictionInstance {

    @Nullable StructureRestrictionInstance getStructureRestrictionInstance();

    void setStructureRestrictionInstance(StructureRestrictionInstance instance);
}
