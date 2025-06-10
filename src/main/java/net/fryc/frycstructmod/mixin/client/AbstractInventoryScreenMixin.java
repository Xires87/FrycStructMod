package net.fryc.frycstructmod.mixin.client;

import com.google.common.collect.Ordering;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.fryc.frycstructmod.util.interfaces.CanHaveStatusEffect;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(AbstractInventoryScreen.class)
abstract class AbstractInventoryScreenMixin<T extends ScreenHandler> extends HandledScreen<T> {

    public AbstractInventoryScreenMixin(T handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Shadow
    private void drawStatusEffectBackgrounds(DrawContext context, int x, int height, Iterable<StatusEffectInstance> statusEffects, boolean wide){}

    @Shadow
    private Text getStatusEffectDescription(StatusEffectInstance statusEffect){return Text.empty();}

    @ModifyExpressionValue(
            method = "drawStatusEffects(Lnet/minecraft/client/gui/DrawContext;II)V",
            at = @At(value = "INVOKE", target = "Ljava/util/Collection;isEmpty()Z")
    )
    private boolean enableDrawingWhenInactiveStatusEffectsArePresent(boolean original) {
        Collection<StatusEffectInstance> collection = ((CanHaveStatusEffect) this.client.player).getInactiveStatusEffects().values();
        return original && collection.isEmpty();
    }


    @Inject(method = "drawStatusEffectSprites(Lnet/minecraft/client/gui/DrawContext;IILjava/lang/Iterable;Z)V", at = @At("HEAD"))
    private void drawInactiveStatusEffectSprites(DrawContext context, int x, int height, Iterable<StatusEffectInstance> statusEffects, boolean wide, CallbackInfo info) {
        Iterable<StatusEffectInstance> iterable = Ordering.natural().sortedCopy(((CanHaveStatusEffect) this.client.player).getInactiveStatusEffects().values());
        StatusEffectSpriteManager statusEffectSpriteManager = this.client.getStatusEffectSpriteManager();
        AtomicInteger i = new AtomicInteger(this.y);
        statusEffects.forEach(effect -> i.addAndGet(height));

        this.drawStatusEffectBackgrounds(context, x, height, iterable, wide);
        for(StatusEffectInstance statusEffectInstance : iterable) {
            StatusEffect statusEffect = statusEffectInstance.getEffectType();
            Sprite sprite = statusEffectSpriteManager.getSprite(statusEffect);
            context.drawSprite(x + (wide ? 6 : 7), i.get() + 7, 0, 18, 18, sprite, 1.0F, 1.0F, 1.0F, 0.50F);

            if(wide){
                Text text = this.getStatusEffectDescription(statusEffectInstance);
                context.drawTextWithShadow(this.textRenderer, text, x + 10 + 18, i.get() + 6, 16777215);
                Text text2 = StatusEffectUtil.getDurationText(statusEffectInstance, 1.0F);
                context.drawTextWithShadow(this.textRenderer, text2, x + 10 + 18, i.get() + 6 + 10, 8355711);
            }


            i.addAndGet(height);
        }
    }


}
