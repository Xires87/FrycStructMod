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

    private final String welcomeMessage;
    private final String leaveMessage;

    public AbstractStructureRestriction(String structureId, String restrictionType, String welcomeMessage, String leaveMessage, RestrictionSource restrictionSource){
        this.structureId = structureId;
        this.restrictionType = restrictionType;
        this.restrictionSource = restrictionSource;
        this.welcomeMessage = welcomeMessage;
        this.leaveMessage = leaveMessage;
    }

    public void executeWhenEnabled(ServerWorld world, BlockPos pos, Structure structure, BiConsumer<StructureStart, StructureRestrictionInstance> consumer){
        this.executeWhenEnabledOrElse(world, pos, structure, consumer, (start, restrictionInstance) -> {});
    }

    public void executeWhenEnabledOrElse(ServerWorld world, BlockPos pos, Structure structure, BiConsumer<StructureStart, StructureRestrictionInstance> consumer, BiConsumer<StructureStart, StructureRestrictionInstance> elseConsumer){
        StructureStart start = world.getStructureAccessor().getStructureAt(pos, structure);
        Optional<StructureRestrictionInstance> opt = ServerRestrictionsHelper.getStructureRestrictionInstance(start);
        opt.ifPresent(restrictionInstance -> {
            if(!restrictionInstance.isRestrictionDisabled(this)){
                consumer.accept(start, restrictionInstance);
            }
            else {
                elseConsumer.accept(start, restrictionInstance);
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

    public String getWelcomeMessage(){
        return this.welcomeMessage;
    }

    public String getLeaveMessage(){
        return this.leaveMessage;
    }
}
