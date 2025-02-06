package net.fryc.frycstructmod.mixin;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fryc.frycstructmod.FrycStructMod;
import net.fryc.frycstructmod.network.ModPackets;
import net.fryc.frycstructmod.util.interfaces.CanBeAffectedByStructure;
import net.fryc.frycstructmod.util.interfaces.HasRestrictions;
import net.fryc.frycstructmod.util.interfaces.HoldsStructureStart;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.Structure;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ServerPlayerEntity.class)
abstract class ServerPlayerEntityMixin extends PlayerEntity implements CanBeAffectedByStructure, HoldsStructureStart {

    @Nullable
    private StructureStart currentStructure = null;

    private int delay = 30;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void test(CallbackInfo info) {

        if(--this.delay < 1){
            ServerWorld world = ((ServerWorld) this.getWorld());
            if(world.getStructureAccessor().hasStructureReferences(this.getBlockPos())){
                Map<Structure, LongSet> structureMap = world.getChunk(this.getBlockPos()).getStructureReferences();

                structureMap.keySet().stream().filter(structure -> {
                    return world.getStructureAccessor().getStructureAt(this.getBlockPos(), structure) != StructureStart.DEFAULT;
                }).findFirst().ifPresentOrElse(structure -> {
                    StructureStart start = world.getStructureAccessor().getStructureAt(this.getBlockPos(), structure);
                    HasRestrictions startWithRestrictions = ((HasRestrictions) (Object) start);
                    if(startWithRestrictions.hasActiveRestrictions()){
                        if(startWithRestrictions.getStructureRestrictionInstance() == null){
                            startWithRestrictions.createStructureRestrictionInstance(world.getRegistryManager());
                        }

                        if(start != this.currentStructure) {
                            Identifier id = world.getRegistryManager().get(RegistryKeys.STRUCTURE).getId(structure);
                            if(id != null){
                                this.currentStructure = start;
                                this.setAffectedByStructureServerAndClient(id.toString());
                                this.sendMessage(Text.of("Weszlem do struktury"));// TODO jakies powiadomienie ze jestes na terenie struktury
                            }
                            else {
                                FrycStructMod.LOGGER.error("Failed to get identifier of the following structure type: " + structure.getType().getClass().getName());
                            }
                        }
                    }
                    else this.resetCurrentStructureWhenNeeded();
                }, this::resetCurrentStructureWhenNeeded);

            }
            else this.resetCurrentStructureWhenNeeded();


            this.delay = 30;
        }


    }

    private void resetCurrentStructureWhenNeeded(){
        if(this.currentStructure != null){
            this.sendMessage(Text.of("Wychodze"));
            this.currentStructure = null;
            this.setAffectedByStructureServerAndClient("");
        }
    }

    public void setAffectedByStructureServerAndClient(String affected) {
        this.setAffectedByStructure(affected);
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(affected);
        ServerPlayNetworking.send(((ServerPlayerEntity) (Object) this), ModPackets.AFFECT_BY_STRUCTURE, buf);
    }

    public StructureStart getStructureStart(){
        return this.currentStructure;
    }
}
