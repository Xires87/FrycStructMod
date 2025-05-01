package net.fryc.frycstructmod.mixin.effect;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StatusEffect.class)
abstract class StatusEffectMixin {

    // entities can be resistant to instant status effects
    @Inject(method = "applyInstantEffect(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/LivingEntity;ID)V", at = @At("HEAD"), cancellable = true)
    private void preventInstantEffectsFromApplyingWhenImmune(@Nullable Entity source, @Nullable Entity attacker, LivingEntity target, int amplifier, double proximity, CallbackInfo info) {
        if(!target.canHaveStatusEffect(new StatusEffectInstance(((StatusEffect)(Object)this)))){
            info.cancel();
        }
    }
}
