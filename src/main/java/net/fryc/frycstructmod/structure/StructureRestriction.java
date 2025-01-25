package net.fryc.frycstructmod.structure;

import net.minecraft.block.Block;

import java.util.Set;

public class StructureRestriction {

    private final String structureId;
    private final boolean allowMining;
    private final boolean allowMiningPlayerBlocks;
    private final boolean allowPlacing;
    private final Set<Block> miningExcludedBlocks;
    private final Set<Block> placingExcludedBlocks;


    public StructureRestriction(String structureId, boolean allowMining, boolean allowMiningPlayerBlocks, Set<Block> miningExcludedBlocks,
                                boolean allowPlacing, Set<Block> placingExcludedBlocks){

        this.structureId = structureId;
        this.allowMining = allowMining;
        this.allowMiningPlayerBlocks = allowMiningPlayerBlocks;
        this.miningExcludedBlocks = miningExcludedBlocks;
        this.allowPlacing = allowPlacing;
        this.placingExcludedBlocks = placingExcludedBlocks;
    }





    public String getStructureId() {
        return this.structureId;
    }

    public boolean isMiningAllowed() {
        return this.allowMining;
    }

    public boolean isMiningAllowedWhenBlockIsPlacedByPlayer() {
        return this.allowMiningPlayerBlocks;
    }

    public boolean isPlacingAllowed() {
        return this.allowPlacing;
    }

    public Set<Block> getMiningExcludedBlocks() {
        return this.miningExcludedBlocks;
    }

    public Set<Block> getPlacingExcludedBlocks() {
        return this.placingExcludedBlocks;
    }
}
