package net.fryc.frycstructmod.structure.restrictions.sources;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class RestrictionSource {

    private final int power;

    private final Set<SourceEntry<?>> entries;


    private RestrictionSource(int power, Set<SourceEntry<?>> entries){
        this.power = power;
        this.entries = entries;
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


    public static class Builder {
        private final ImmutableSet.Builder<SourceEntry<?>> sourceEntries = ImmutableSet.builder();
        private int power = 1;

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

        public RestrictionSource build(){
            RestrictionSource source = new RestrictionSource(this.power, this.sourceEntries.build());
            for(SourceEntry<?> entry : source.getEntries()){
                entry.setOwner(source);
            }
            return source;
        }

    }
}
