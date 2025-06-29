package net.fryc.frycstructmod.structure.restrictions.sources;

import net.fryc.frycstructmod.structure.restrictions.StructureRestrictionInstance;
import net.fryc.frycstructmod.structure.restrictions.sources.events.Event;
import net.fryc.frycstructmod.structure.restrictions.sources.events.SourceEntryEvent;
import net.fryc.frycstructmod.util.ModProperties;
import net.fryc.frycstructmod.util.ServerRestrictionsHelper;
import net.fryc.frycstructmod.util.interfaces.HasRestrictions;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;

public class BlockStateSourceEntry extends AbstractSourceEntry<BlockState> {


    public BlockStateSourceEntry(Identifier sourceId, int sourceStrength) {
        super(sourceId, sourceStrength);
    }

    /**
     *  Returns true when restriction's power decreases
     */
    @Override
    public boolean affectOwner(StructureStart structureStart, BlockState source, PlayerEntity player) {
        if(source.getProperties().contains(ModProperties.PLACED_BY_PLAYER)){
            if(source.get(ModProperties.PLACED_BY_PLAYER)){
                return false;
            }
        }

        if(Registries.BLOCK.getId(source.getBlock()).equals(this.sourceId)){
            StructureRestrictionInstance instance = ((HasRestrictions) (Object) structureStart).getStructureRestrictionInstance();
            if(instance != null){
                if(instance.decreaseCurrentPower(this.sourceStrength, this)){
                    instance.updateDisabledRestrictions();
                    ServerRestrictionsHelper.tryToRemoveRestrictionsFromStructure(structureStart, instance);
                }

                return true;
            }
        }

        return false;
    }


    @Override
    public Event getEvent() {
        return SourceEntryEvent.ON_BLOCK_DESTROY;
    }

}
