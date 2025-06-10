package net.fryc.frycstructmod.structure.restrictions.sources;

import com.google.common.collect.ImmutableSet;
import net.fryc.frycstructmod.FrycStructMod;

import java.util.Set;

public class RestrictionSource {

    private final int power;
    private final ShareOperation shareOperation;
    private final boolean shared;

    private final Set<SourceEntry<?>> entries;


    private RestrictionSource(int power, Set<SourceEntry<?>> entries, boolean shared, ShareOperation shareOperation){
        this.power = power;
        this.entries = entries;
        this.shared = shared;
        this.shareOperation = shareOperation;
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

    public ShareOperation getShareOperation(){
        return this.shareOperation;
    }


    public static class Builder {
        private final ImmutableSet.Builder<SourceEntry<?>> sourceEntries = ImmutableSet.builder();
        private int power = 1;
        private boolean shared = false;
        private ShareOperation shareOperation = ShareOperation.MAX;

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

        /**
         *  Has to be set to "max", "add" or "mul"
         */
        public Builder setShareOperation(String operation){
            ShareOperation op = getShareOperation(operation);
            if(op != null){
                this.shareOperation = op;
            }
            else {
                FrycStructMod.LOGGER.error("Error occured while setting share operation for restriction source. '" + operation +
                        "' is not a valid operation. Expected one of the following operations: 'max', 'add', 'mul'");
            }

            return this;
        }

        public RestrictionSource build(){
            RestrictionSource source = new RestrictionSource(this.power, this.sourceEntries.build(), this.shared, this.shareOperation);
            for(SourceEntry<?> entry : source.getEntries()){
                entry.setOwner(source);
            }
            return source;
        }

        private static ShareOperation getShareOperation(String operation){
            return switch (operation){
                case "max" -> ShareOperation.MAX;
                case "add" -> ShareOperation.ADD;
                case "mul" -> ShareOperation.MUL;
                default -> null;
            };
        }
    }
}
