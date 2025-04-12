package net.fryc.frycstructmod.util.interfaces;

import net.minecraft.structure.StructureStart;
import org.jetbrains.annotations.Nullable;

public interface HoldsStructureStart {

    @Nullable StructureStart getStructureStart();

    void setStructureStart(@Nullable StructureStart start);
}
