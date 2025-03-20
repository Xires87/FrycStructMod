package net.fryc.frycstructmod.util.interfaces;


import org.jetbrains.annotations.Nullable;

public interface CanBeAffectedByStructure {

    boolean isAffectedByStructure();

    void setAffectedByStructure(@Nullable String affected);

    String getStructureId();

    // TODO podmienic zeby instancja przechowywala wylaczone restrykcje a nie gracz bo to nie wypali
}
