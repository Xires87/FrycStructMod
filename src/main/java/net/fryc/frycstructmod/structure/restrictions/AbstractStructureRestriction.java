package net.fryc.frycstructmod.structure.restrictions;

import net.fryc.frycstructmod.structure.restrictions.sources.RestrictionSource;
import net.fryc.frycstructmod.util.ServerRestrictionsHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.Structure;

import java.util.Optional;
import java.util.function.BiConsumer;


public abstract class AbstractStructureRestriction {

    private final String structureId;
    private final String restrictionType;
    private final RestrictionSource restrictionSource;

    public AbstractStructureRestriction(String structureId, String restrictionType, RestrictionSource restrictionSource){
        this.structureId = structureId;
        this.restrictionType = restrictionType;
        this.restrictionSource = restrictionSource;
    }

    public void executeWhenEnabled(ServerWorld world, BlockPos pos, Structure structure, BiConsumer<StructureStart, StructureRestrictionInstance> consumer){
        StructureStart start = world.getStructureAccessor().getStructureAt(pos, structure);
        Optional<StructureRestrictionInstance> opt = ServerRestrictionsHelper.getStructureRestrictionInstance(start);
        opt.ifPresent(restrictionInstance -> {
            if(!restrictionInstance.isRestrictionDisabled(this)){
                consumer.accept(start, restrictionInstance);
            }
        });
    }

    public String getStructureId() {
        return this.structureId;
    }

    public String getRestrictionType(){
        return this.restrictionType;
    }

    public RestrictionSource getRestrictionSource(){
        return this.restrictionSource;
    }
}
