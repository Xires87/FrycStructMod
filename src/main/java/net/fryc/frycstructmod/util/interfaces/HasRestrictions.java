package net.fryc.frycstructmod.util.interfaces;

import net.fryc.frycstructmod.structure.restrictions.StructureRestrictionInstance;
import net.minecraft.registry.DynamicRegistryManager;
import org.jetbrains.annotations.Nullable;

public interface HasRestrictions {

    boolean hasActiveRestrictions();

    void setActiveRestrictions(boolean active);

    @Nullable StructureRestrictionInstance getStructureRestrictionInstance();

    void createStructureRestrictionInstance(DynamicRegistryManager manager);

}
