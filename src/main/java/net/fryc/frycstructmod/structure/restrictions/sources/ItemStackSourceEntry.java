package net.fryc.frycstructmod.structure.restrictions.sources;

import net.fryc.frycstructmod.structure.restrictions.StructureRestrictionInstance;
import net.fryc.frycstructmod.structure.restrictions.sources.events.Event;
import net.fryc.frycstructmod.structure.restrictions.sources.events.SourceEntryEvent;
import net.fryc.frycstructmod.util.ServerRestrictionsHelper;
import net.fryc.frycstructmod.util.interfaces.HasRestrictions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;

public class ItemStackSourceEntry extends AbstractSourceEntry<ItemStack> {

    public ItemStackSourceEntry(Identifier sourceId, int sourceStrength) {
        super(sourceId, sourceStrength);
    }

    @Override
    public boolean affectOwner(StructureStart structureStart, ItemStack source, PlayerEntity player) {
        if(Registries.ITEM.getId(source.getItem()).equals(this.sourceId)){
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
        return SourceEntryEvent.ON_ITEM_USE_FINISH;
    }
}
