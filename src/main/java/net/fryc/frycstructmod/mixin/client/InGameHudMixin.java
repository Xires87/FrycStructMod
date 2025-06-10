package net.fryc.frycstructmod.mixin.client;

import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    /*

    @Shadow
    private @Final MinecraftClient client;

    @Shadow
    private int scaledWidth;


    @Inject(method = "renderStatusEffectOverlay(Lnet/minecraft/client/gui/DrawContext;)V", at = @At("HEAD"))
    private void renderStatusEffectOverlay(DrawContext context, CallbackInfo info) {
        renderInactiveStatusEffectOverlay(context);
    }


    protected void renderInactiveStatusEffectOverlay(DrawContext context) {
        Collection<StatusEffectInstance> collection = ((CanHaveStatusEffect) this.client.player).getInactiveStatusEffects().values();
        if (!collection.isEmpty()) {
            Screen screen = this.client.currentScreen;
            if (screen instanceof AbstractInventoryScreen abstractInventoryScreen) {
                if (abstractInventoryScreen.hideStatusEffectHud()) {
                    return;
                }
            }

            RenderSystem.enableBlend();
            int i = 0;
            int j = 0;
            StatusEffectSpriteManager statusEffectSpriteManager = this.client.getStatusEffectSpriteManager();
            List<Runnable> list = Lists.newArrayListWithExpectedSize(collection.size());

            for(StatusEffectInstance statusEffectInstance : Ordering.natural().reverse().sortedCopy(collection)) {
                StatusEffect statusEffect = statusEffectInstance.getEffectType();
                if (statusEffectInstance.shouldShowIcon()) {
                    int k = this.scaledWidth;
                    int l = 1;
                    if (this.client.isDemo()) {
                        l += 15;
                    }

                    if (statusEffect.isBeneficial()) {
                        ++i;
                        k -= 25 * i;
                    } else {
                        ++j;
                        k -= 25 * j;
                        l += 26;
                    }

                    float f = 1.0F;
                    if (statusEffectInstance.isAmbient()) {
                        context.drawTexture(HandledScreen.BACKGROUND_TEXTURE, k, l, 165, 166, 24, 24);
                    } else {
                        context.drawTexture(HandledScreen.BACKGROUND_TEXTURE, k, l, 141, 166, 24, 24);
                        if (statusEffectInstance.isDurationBelow(200)) {
                            int m = statusEffectInstance.getDuration();
                            int n = 10 - m / 20;
                            f = MathHelper.clamp((float)m / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F) + MathHelper.cos((float)m * (float)Math.PI / 5.0F) * MathHelper.clamp((float)n / 10.0F * 0.25F, 0.0F, 0.25F);
                        }
                    }

                    Sprite sprite = statusEffectSpriteManager.getSprite(statusEffect);
                    int finalK = k;
                    int finalL = l;
                    list.add((Runnable)() -> {
                        context.setShaderColor(1.0F, 1.0F, 1.0F, 0.20F);
                        context.drawSprite(finalK + 3, finalL + 3, 0, 18, 18, sprite);
                        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    });
                }
            }

            list.forEach(Runnable::run);
        }
    }

     */
}
