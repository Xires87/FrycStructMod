package net.fryc.frycstructmod.structure.restrictions.sources;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class RestrictionSource {

    private final int power;
    private final boolean shared;

    private final Set<SourceEntry<?>> entries;


    private RestrictionSource(int power, Set<SourceEntry<?>> entries, boolean shared){
        this.power = power;
        this.entries = entries;
        this.shared = shared;
    }

    // to make sure all entries have their owner
    public static Builder builder(){
        return new Builder();
    }


    public Set<SourceEntry<?>> getEntries(){
        return this.entries;
    }

    public int getPower(){
        return this.power;
    }

    public boolean isShared(){
        return this.shared;
    }


    public static class Builder {
        private final ImmutableSet.Builder<SourceEntry<?>> sourceEntries = ImmutableSet.builder();
        private int power = 1;
        private boolean shared = false;

        private Builder(){
        }

        public Builder putAllSourceEntries(Iterable<SourceEntry<?>> entries){
            this.sourceEntries.addAll(entries);
            return this;
        }

        public Builder putSourceEntry(SourceEntry<?> entry){
            this.sourceEntries.add(entry);
            return this;
        }

        public Builder setPower(int power){
            this.power = power;
            return this;
        }

        public Builder setShared(boolean shared){
            this.shared = shared;
            return this;
        }

        public RestrictionSource build(){
            RestrictionSource source = new RestrictionSource(this.power, this.sourceEntries.build(), this.shared);
            for(SourceEntry<?> entry : source.getEntries()){
                entry.setOwner(source);
            }
            return source;
        }

    }
}
