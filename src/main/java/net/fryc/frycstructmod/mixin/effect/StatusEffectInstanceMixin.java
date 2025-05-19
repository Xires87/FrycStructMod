package net.fryc.frycstructmod.mixin.effect;

import net.fryc.frycstructmod.util.interfaces.StructureStatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StatusEffectInstance.class)
abstract class StatusEffectInstanceMixin implements StructureStatusEffect {

    @Shadow
    @Nullable
    private StatusEffectInstance hiddenEffect;
// TODO musze to do nbt zapisywac bo jak wyjde to zostaje efekt na zawsze
    public boolean fromStructure = false;

    public boolean isFromStructure() {
        return this.fromStructure;
    }

    public void setFromStructure(boolean fromStructure) {
        this.fromStructure = fromStructure;
    }

    public @Nullable StatusEffectInstance getHiddenEffect(){
        return this.hiddenEffect;
    }

    public void setHiddenEffect(@Nullable StatusEffectInstance instance){
        this.hiddenEffect = instance;
    }

    @Inject(method = "copyFrom(Lnet/minecraft/entity/effect/StatusEffectInstance;)V", at = @At("HEAD"))
    private void copyFromStructureValue(StatusEffectInstance that, CallbackInfo info) {
        this.fromStructure = ((StructureStatusEffect) that).isFromStructure();
    }
}
