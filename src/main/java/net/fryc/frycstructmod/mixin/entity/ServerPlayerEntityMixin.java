package net.fryc.frycstructmod.mixin.entity;

import com.mojang.authlib.GameProfile;
import net.fryc.frycstructmod.FrycStructMod;
import net.fryc.frycstructmod.util.ServerRestrictionsHelper;
import net.fryc.frycstructmod.util.interfaces.CanBeAffectedByStructure;
import net.fryc.frycstructmod.util.interfaces.HasRestrictions;
import net.fryc.frycstructmod.util.interfaces.HoldsStructureStart;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
abstract class ServerPlayerEntityMixin extends PlayerEntity implements CanBeAffectedByStructure, HoldsStructureStart {

    @Nullable
    private StructureStart currentStructure = null;

    private int delay = 120;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void checkForStructureEnter(CallbackInfo info) {

        if(--this.delay < 1){
            ServerWorld world = ((ServerWorld) this.getWorld());
            ServerRestrictionsHelper.executeIfHasStructureOrElse(world, this.getBlockPos(), structure -> {

                StructureStart start = world.getStructureAccessor().getStructureAt(this.getBlockPos(), structure);
                HasRestrictions startWithRestrictions = ((HasRestrictions) (Object) start);

                if(startWithRestrictions.hasActiveRestrictions()){
                    if(startWithRestrictions.getStructureRestrictionInstance() == null){
                        startWithRestrictions.createStructureRestrictionInstance(world.getRegistryManager());
                    }

                    // second check, because createStructureRestrictionInstance([...]); can disable restrictions
                    if(startWithRestrictions.hasActiveRestrictions()){
                        Identifier id = world.getRegistryManager().get(RegistryKeys.STRUCTURE).getId(structure);
                        if(id != null){
                            if(start != this.currentStructure) {
                                if(!ServerRestrictionsHelper.tryToRemoveRestrictionsFromStructure(start, startWithRestrictions.getStructureRestrictionInstance())){
                                    this.currentStructure = start;
                                    ServerRestrictionsHelper.setAffectedByStructureServerAndClient(this, id.toString(), startWithRestrictions.getStructureRestrictionInstance());
                                    this.sendMessage(Text.of("Weszlem do struktury"));// TODO jakies FAJNE powiadomienie ze jestes na terenie struktury

                                    // checks for persistent entities on enter in case they somehow died (without player's help)
                                    ServerRestrictionsHelper.checkForPersistentEntitiesFromSource(startWithRestrictions.getStructureRestrictionInstance(), world, start);
                                }
                            }

                            world.getOtherEntities(this, Box.from(start.getBoundingBox()), entity -> {
                                return entity instanceof LivingEntity living && living.isAlive() &&
                                        !living.isPlayer() && !((CanBeAffectedByStructure)living).isAffectedByStructure();
                            }).forEach(entity -> {
                                ((CanBeAffectedByStructure) entity).setAffectedByStructure(id.toString());
                            });
                        }
                        else {
                            FrycStructMod.LOGGER.error("Failed to get identifier of the following structure type: " + structure.getType().getClass().getName());
                        }
                    }
                }
                else {
                    this.resetCurrentStructureWhenNeeded();
                }
            }, this::resetCurrentStructureWhenNeeded);

            this.delay = 30;
        }


    }

    private void resetCurrentStructureWhenNeeded(){
        if(this.currentStructure != null){
            this.getWorld().getChunk(this.currentStructure.getPos().x, this.currentStructure.getPos().z).setNeedsSaving(true);
            this.sendMessage(Text.of("Wychodze"));
            this.currentStructure = null;
            ServerRestrictionsHelper.setAffectedByStructureServerAndClient(this, "", null);
        }
    }

    public @Nullable StructureStart getStructureStart(){
        return this.currentStructure;
    }
}
