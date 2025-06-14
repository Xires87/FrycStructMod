package net.fryc.frycstructmod.util.interfaces;


import org.jetbrains.annotations.Nullable;

public interface CanBeAffectedByStructure {

    boolean isAffectedByStructure();

    void setAffectedByStructure(@Nullable String affected);

    String getStructureId();

    void setLeaveMessage(String message);

    String getLeaveMessage();

}
