package net.fryc.frycstructmod.util.interfaces;


import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface CanBeAffectedByStructure {

    boolean isAffectedByStructure();

    void setAffectedByStructure(@Nullable String affected);

    String getStructureId();

    Set<String> getRestrictionsImmuneTo();

    boolean shouldBeAffectedByRestriction(String restrictionType);
}
