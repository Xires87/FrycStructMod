package net.fryc.frycstructmod.util.interfaces;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.HashMap;

public interface CanHaveStatusEffect {

    HashMap<StatusEffect, StatusEffectInstance> getInactiveStatusEffects();

    void addStatusEffectToInactiveEffects(StatusEffectInstance effect);
}
