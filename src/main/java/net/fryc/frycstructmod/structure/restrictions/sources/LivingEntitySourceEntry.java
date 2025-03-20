package net.fryc.frycstructmod.structure.restrictions.sources;

import net.fryc.frycstructmod.structure.restrictions.StructureRestrictionInstance;
import net.fryc.frycstructmod.structure.restrictions.sources.events.Event;
import net.fryc.frycstructmod.structure.restrictions.sources.events.SourceEntryEvent;
import net.fryc.frycstructmod.util.ServerRestrictionsHelper;
import net.fryc.frycstructmod.util.interfaces.HasRestrictions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;

public class LivingEntitySourceEntry extends AbstractSourceEntry<LivingEntity>{


    public LivingEntitySourceEntry(Identifier sourceId, int sourceStrength) {
        super(sourceId, sourceStrength);
    }

    @Override
    public boolean affectOwner(StructureStart structureStart, LivingEntity source, PlayerEntity player) {
        if(Registries.ENTITY_TYPE.getId(source.getType()).equals(this.sourceId)){
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
        return SourceEntryEvent.ON_MOB_KILL;
    }
}
