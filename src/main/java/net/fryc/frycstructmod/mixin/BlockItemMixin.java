package net.fryc.frycstructmod.mixin;

import net.fryc.frycstructmod.util.ModProperties;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BlockItem.class)
abstract class BlockItemMixin {


    @ModifyVariable(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At("STORE"), ordinal = 0)
    private BlockState modifyPlacedByPlayerProperty(BlockState blockState, ItemPlacementContext context) {
        if(context.getPlayer() != null && blockState != null){
            return blockState.withIfExists(ModProperties.PLACED_BY_PLAYER, true);
        }
        return blockState;
    }

}
