package net.fryc.frycstructmod.util.interfaces;

import net.fryc.frycstructmod.structure.restrictions.StructureRestrictionInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.DynamicRegistryManager;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface HasRestrictions {

    boolean hasActiveRestrictions();

    void setActiveRestrictions(boolean active);

    @Nullable StructureRestrictionInstance getStructureRestrictionInstance();

    void createStructureRestrictionInstance(DynamicRegistryManager manager);

    void tryToDisableRestrictionsAndUpdateRestrictionImmunity(StructureRestrictionInstance instance, PlayerEntity player);

    boolean tryToDisableRestrictions(Set<String> instanceRestrictions, Set<String> disabledRestrictions);
}
