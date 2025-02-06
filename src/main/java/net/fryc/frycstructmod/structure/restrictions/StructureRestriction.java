package net.fryc.frycstructmod.structure.restrictions;

import net.fryc.frycstructmod.structure.restrictions.sources.RestrictionSource;
import net.fryc.frycstructmod.util.RestrictionsHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;

import java.util.Set;

public class StructureRestriction {

    private final String structureId;
    private final boolean allowMining;
    private final boolean allowMiningPlayerBlocks;
    private final boolean allowPlacing;
    private final Set<Block> miningExcludedBlocks;
    private final Set<Block> placingExcludedBlocks;
    private final RestrictionSource restrictionSource;

    public StructureRestriction(String structureId, boolean allowMining, boolean allowMiningPlayerBlocks, Set<Block> miningExcludedBlocks,
                                boolean allowPlacing, Set<Block> placingExcludedBlocks, RestrictionSource restrictionSource){

        this.structureId = structureId;
        this.allowMining = allowMining;
        this.allowMiningPlayerBlocks = allowMiningPlayerBlocks;
        this.miningExcludedBlocks = miningExcludedBlocks;
        this.allowPlacing = allowPlacing;
        this.placingExcludedBlocks = placingExcludedBlocks;
        this.restrictionSource = restrictionSource;
    }


    public float modifyBlockBreakingSpeedWhenNeeded(float originalSpeed, BlockState block, PlayerEntity playerEntity){
        if(this.isMiningAllowedWhenBlockIsPlacedByPlayer() && RestrictionsHelper.isPlacedByPlayer(block)){
            return originalSpeed;
        }
        else if(this.isMiningAllowed() == this.getMiningExcludedBlocks().contains(block.getBlock())){
            return originalSpeed * 0.00027F;
        }

        return originalSpeed;
    }

    public boolean canBePlaced(BlockState block, ItemUsageContext context){
        return this.isPlacingAllowed() != this.getPlacingExcludedBlocks().contains(block.getBlock());
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

    public RestrictionSource getRestrictionSource(){
        return this.restrictionSource;
    }
}
