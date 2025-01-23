package net.fryc.frycstructmod.mixin;


import net.fryc.frycstructmod.blocks.ModProperties;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
abstract class BlockMixin {

    @Shadow
    protected abstract @Final void setDefaultState(BlockState state);

    @Shadow
    public abstract @Final BlockState getDefaultState();


    @Inject(method = "<init>(Lnet/minecraft/block/AbstractBlock$Settings;)V", at = @At("TAIL"))
    private void setPlacedByPlayerPropertyDefaultValue(AbstractBlock.Settings settings, CallbackInfo info) {
        this.setDefaultState(this.getDefaultState().withIfExists(ModProperties.PLACED_BY_PLAYER, false));
    }


    @Inject(method = "appendProperties(Lnet/minecraft/state/StateManager$Builder;)V", at = @At("HEAD"))
    private void addPlacedByPlayerProperty(StateManager.Builder<Block, BlockState> builder, CallbackInfo info) {
        builder.add(ModProperties.PLACED_BY_PLAYER);
        // TODO dodac jakos ten property do WSZYSTKICH blokow (appendProperties jest overridowane w wielu blokach, a w konstruktorze chyba nie moge zmieniac localsow)
    }

}
