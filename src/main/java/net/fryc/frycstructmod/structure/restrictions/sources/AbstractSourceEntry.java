package net.fryc.frycstructmod.structure.restrictions.sources;

import net.minecraft.util.Identifier;

public abstract class AbstractSourceEntry<T> implements SourceEntry<T> {

    protected RestrictionSource owner;

    protected final int sourceStrength;

    protected final Identifier sourceId;

    public AbstractSourceEntry(Identifier sourceId, int sourceStrength){
        this.sourceId = sourceId;
        this.sourceStrength = sourceStrength;
    }

    public void setOwner(RestrictionSource owner){
        this.owner = owner;
    }
}
