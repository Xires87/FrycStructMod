package net.fryc.frycstructmod.structure.restrictions;

import net.fryc.frycstructmod.structure.restrictions.sources.RestrictionSource;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import oshi.util.tuples.Pair;

import java.util.Map;
import java.util.Set;

public class StatusEffectStructureRestriction extends AbstractStructureRestriction {

    private final boolean affectAllEntities;
    private final boolean allowAllEffects;

    private final Set<StatusEffect> excludedEffects;
    private final Set<LivingEntity> excludedEntities;
    private final Map<StatusEffect, Pair<Integer, Integer>> persistentEffects;


    public StatusEffectStructureRestriction(String structureId, boolean affectAllEntities, Set<LivingEntity> excludedEntities,
                                            boolean allowAllEffects, Set<StatusEffect> excludedEffects,
                                            Map<StatusEffect, Pair<Integer, Integer>> persistentEffects, RestrictionSource restrictionSource) {

        super(structureId, restrictionSource);
        this.affectAllEntities = affectAllEntities;
        this.allowAllEffects = allowAllEffects;
        this.excludedEffects = excludedEffects;
        this.excludedEntities = excludedEntities;
        this.persistentEffects = persistentEffects;
    }
// TODO zarejestrowac to i wstawic odpalanie w odpowiednim miejscu

    public Set<StatusEffect> getExcludedEffects() {
        return excludedEffects;
    }

    public boolean shouldAffectAllEntities() {
        return affectAllEntities;
    }

    public boolean shouldAllowAllEffects() {
        return allowAllEffects;
    }

    public Set<LivingEntity> getExcludedEntities() {
        return excludedEntities;
    }

    public Map<StatusEffect, Pair<Integer, Integer>> getPersistentEffects() {
        return persistentEffects;
    }

}
