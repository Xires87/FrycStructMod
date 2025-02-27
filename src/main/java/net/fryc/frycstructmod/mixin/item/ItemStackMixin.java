package net.fryc.frycstructmod.mixin.item;

import net.fabricmc.fabric.api.item.v1.FabricItemStack;
import net.fryc.frycstructmod.structure.restrictions.sources.events.SourceEntryEvent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
abstract class ItemStackMixin implements FabricItemStack {

    @Inject(method = "finishUsing(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/item/ItemStack;", at = @At("HEAD"))
    private void onFinishUsing(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> ret) {
        if(!world.isClient()){
            if(user instanceof PlayerEntity player){
                SourceEntryEvent.ON_ITEM_USE_FINISH.triggerEvent(((ItemStack)(Object)this), player, (ServerWorld) world, player.getBlockPos());
            }
        }
    }
}
