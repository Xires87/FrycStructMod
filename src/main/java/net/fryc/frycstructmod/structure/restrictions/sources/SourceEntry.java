package net.fryc.frycstructmod.structure.restrictions.sources;

public interface SourceEntry<T> {


    boolean affectOwner(T source);

    Class<T> getEntryClass();


    void setOwner(RestrictionSource owner);

}
