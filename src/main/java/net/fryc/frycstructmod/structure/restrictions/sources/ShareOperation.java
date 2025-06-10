package net.fryc.frycstructmod.structure.restrictions.sources;

public enum ShareOperation {

    MAX {
        @Override
        public int applyOperation(int currentValue, int nextValue) {
            return Math.max(currentValue, nextValue);
        }
    },
    ADD {
        @Override
        public int applyOperation(int currentValue, int nextValue) {
            return currentValue + nextValue;
        }
    },
    MUL {
        @Override
        public int applyOperation(int currentValue, int nextValue) {
            return currentValue * nextValue;
        }
    };

    public abstract int applyOperation(int currentValue, int nextValue);
}
