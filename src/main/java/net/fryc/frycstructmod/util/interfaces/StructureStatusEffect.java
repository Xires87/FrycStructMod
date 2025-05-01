package net.fryc.frycstructmod.util.interfaces;

import net.minecraft.entity.effect.StatusEffectInstance;
import org.jetbrains.annotations.Nullable;

public interface StructureStatusEffect {

    boolean isFromStructure();

    void setFromStructure(boolean fromStructure);

    @Nullable StatusEffectInstance getHiddenEffect();

    void setHiddenEffect(@Nullable StatusEffectInstance instance);
}
