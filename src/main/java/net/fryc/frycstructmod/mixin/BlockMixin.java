package net.fryc.frycstructmod.mixin;


import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.fabricmc.fabric.api.block.v1.FabricBlock;
import net.fryc.frycstructmod.structure.restrictions.sources.events.SourceEntryEvent;
import net.fryc.frycstructmod.util.ModProperties;
import net.fryc.frycstructmod.util.RestrictionsHelper;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Block.class)
abstract class BlockMixin extends AbstractBlock implements ItemConvertible, FabricBlock {

    @Shadow
    protected @Final StateManager<Block, BlockState> stateManager;

    public BlockMixin(Settings settings) {
        super(settings);
    }

    @Shadow
    protected abstract @Final void setDefaultState(BlockState state);

    @Shadow
    public abstract @Final BlockState getDefaultState();


    @ModifyVariable(method = {"<init>"}, at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/state/StateManager$Builder;build(Ljava/util/function/Function;Lnet/minecraft/state/StateManager$Factory;)Lnet/minecraft/state/StateManager;"
    ))
    private StateManager.Builder<Block, BlockState> addPlacedByPlayerProperty(StateManager.Builder<Block, BlockState> builder) {
        if(builder != null){
            if(builder.build(Block::getDefaultState, BlockState::new).getProperties().size() < ModProperties.BLOCK_PROPERTIES_CAP){
                builder.add(new Property[]{ModProperties.PLACED_BY_PLAYER});
            }
        }
        return builder;
    }



    @Inject(method = "<init>(Lnet/minecraft/block/AbstractBlock$Settings;)V", at = @At("TAIL"))
    private void setPlacedByPlayerPropertyDefaultValue(Settings settings, CallbackInfo info) {
        this.setDefaultState(this.stateManager.getDefaultState().withIfExists(ModProperties.PLACED_BY_PLAYER, false));
    }


    @ModifyReturnValue(method = "getDefaultState()Lnet/minecraft/block/BlockState;", at = @At("RETURN"))
    private BlockState setPlacedByPlayerPropertyDefaultValueOnGetter(BlockState original) {
        return original.withIfExists(ModProperties.PLACED_BY_PLAYER, false);
    }

    @Inject(method = "onBreak(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)V", at = @At("HEAD"))
    public void onBlockBreak(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo info) {
        if(!world.isClient()){
            SourceEntryEvent.ON_BLOCK_DESTROY.triggerEvent(state, player, (ServerWorld) world, pos);
        }
    }


}
