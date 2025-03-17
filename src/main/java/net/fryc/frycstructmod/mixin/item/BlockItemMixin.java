package net.fryc.frycstructmod.mixin.item;

import net.fryc.frycstructmod.structure.restrictions.AbstractStructureRestriction;
import net.fryc.frycstructmod.structure.restrictions.DefaultStructureRestriction;
import net.fryc.frycstructmod.util.ModProperties;
import net.fryc.frycstructmod.util.RestrictionsHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(BlockItem.class)
abstract class BlockItemMixin {

    @Shadow
    @Nullable protected abstract BlockState getPlacementState(ItemPlacementContext context);

    @Shadow
    @Nullable public abstract ItemPlacementContext getPlacementContext(ItemPlacementContext context);


    @ModifyVariable(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At("STORE"), ordinal = 0)
    private BlockState modifyPlacedByPlayerProperty(BlockState blockState, ItemPlacementContext context) {
        if(context.getPlayer() != null && blockState != null){
            return blockState.withIfExists(ModProperties.PLACED_BY_PLAYER, true);
        }
        return blockState;
    }

    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At("HEAD"), cancellable = true)
    private void disallowPlacingWhenAffectedByStructure(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> ret) {
        PlayerEntity player = context.getPlayer();
        if(player != null){
            Optional<AbstractStructureRestriction> optional = RestrictionsHelper.getRestrictionByTypeIfAffectsPlayer("default", player);
            if(optional.isPresent()){
                ItemPlacementContext itemPlacementContext = this.getPlacementContext(context);
                if(itemPlacementContext != null){
                    BlockState blockState = this.getPlacementState(itemPlacementContext);
                    if(blockState != null){
                        if(!((DefaultStructureRestriction) optional.get()).canBePlaced(blockState, itemPlacementContext)){
                            ret.setReturnValue(ActionResult.FAIL);
                        }
                    }
                }
            }
        }
    }

}
