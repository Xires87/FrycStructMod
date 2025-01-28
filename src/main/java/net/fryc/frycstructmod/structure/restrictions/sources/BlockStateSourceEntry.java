package net.fryc.frycstructmod.structure.restrictions.sources;

import net.fryc.frycstructmod.util.ModProperties;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class BlockStateSourceEntry extends AbstractSourceEntry<BlockState> {


    public BlockStateSourceEntry(Identifier sourceId, int sourceStrength) {
        super(sourceId, sourceStrength);
    }

    @Override
    public boolean affectOwner(BlockState source) {
        if(source.getProperties().contains(ModProperties.PLACED_BY_PLAYER)){
            if(source.get(ModProperties.PLACED_BY_PLAYER)){
                return false;
            }
        }

        if(Registries.BLOCK.getId(source.getBlock()).equals(this.sourceId)){
            this.owner.decreasePower(this.sourceStrength);
            return true;
        }

        return false;
    }

    @Override
    public Class<BlockState> getEntryClass() {
        return BlockState.class;
    }

}
