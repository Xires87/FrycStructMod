package net.fryc.frycstructmod.structure.restrictions;

import net.fryc.frycstructmod.structure.restrictions.sources.RestrictionSource;
import net.fryc.frycstructmod.util.ModProperties;
import net.fryc.frycstructmod.util.RestrictionsHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;

import java.util.Set;

public class DefaultStructureRestriction extends AbstractStructureRestriction {

    private final boolean allowMining;
    private final boolean allowMiningPlayerBlocks;
    private final boolean allowPlacing;
    private final boolean disallowPlacingIndestructibleBlocks;
    private final Set<Block> miningExcludedBlocks;
    private final Set<Block> placingExcludedBlocks;
    private final float miningSpeedMultiplier = 0.00027F;// TODO dac to do konstruktora zeby mozna bylo w jsonie ustawiac

    public DefaultStructureRestriction(String structureId, boolean allowMining, boolean allowMiningPlayerBlocks, Set<Block> miningExcludedBlocks,
                                       boolean allowPlacing, boolean alwaysDisallowPlacingIndestructibleBlocks, Set<Block> placingExcludedBlocks, RestrictionSource restrictionSource){

        super(structureId, restrictionSource);
        this.allowMining = allowMining;
        this.allowMiningPlayerBlocks = allowMiningPlayerBlocks;
        this.miningExcludedBlocks = miningExcludedBlocks;
        this.allowPlacing = allowPlacing;
        this.disallowPlacingIndestructibleBlocks = alwaysDisallowPlacingIndestructibleBlocks;
        this.placingExcludedBlocks = placingExcludedBlocks;
    }


    public float modifyBlockBreakingSpeedWhenNeeded(float originalSpeed, BlockState block, PlayerEntity playerEntity){
        if(this.shouldMineBlockPlacedByPlayer(block, playerEntity)){
            return originalSpeed;
        }
        else if(!this.canBeMined(block, playerEntity)){
            return originalSpeed * this.miningSpeedMultiplier;
        }

        return originalSpeed;
    }

    public boolean canBeMined(BlockState block, PlayerEntity playerEntity){
        return this.isMiningAllowed() != this.getMiningExcludedBlocks().contains(block.getBlock());
    }

    public boolean shouldMineBlockPlacedByPlayer(BlockState block, PlayerEntity playerEntity){
        return this.isMiningAllowedWhenBlockIsPlacedByPlayer() && RestrictionsHelper.isPlacedByPlayer(block);
    }

    public boolean canBePlaced(BlockState block, ItemUsageContext context){
        if(this.isPlacingIndestructibleBlocksDisallowed()){
            if(!(block.getProperties().contains(ModProperties.PLACED_BY_PLAYER) && this.isMiningAllowedWhenBlockIsPlacedByPlayer())){
                if(!canBeMined(block, context.getPlayer())){
                    return false;
                }
            }
        }
        return this.isPlacingAllowed() != this.getPlacingExcludedBlocks().contains(block.getBlock());
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

    public boolean isPlacingIndestructibleBlocksDisallowed() {
        return this.disallowPlacingIndestructibleBlocks;
    }

    public Set<Block> getMiningExcludedBlocks() {
        return this.miningExcludedBlocks;
    }

    public Set<Block> getPlacingExcludedBlocks() {
        return this.placingExcludedBlocks;
    }

}
