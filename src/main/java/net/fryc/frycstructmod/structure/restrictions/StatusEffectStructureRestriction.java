package net.fryc.frycstructmod.structure.restrictions;

import net.fryc.frycstructmod.structure.restrictions.registry.RestrictionTypes;
import net.fryc.frycstructmod.structure.restrictions.sources.RestrictionSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import oshi.util.tuples.Quartet;
import oshi.util.tuples.Triplet;

import java.util.Map;
import java.util.Set;

public class StatusEffectStructureRestriction extends AbstractStructureRestriction {

    private final boolean affectAllEntities;
    private final boolean allowAllEffects;

    private final Set<StatusEffect> excludedEffects;
    private final Set<EntityType<?>> excludedEntities;
    private final Map<StatusEffect, Triplet<Quartet<Boolean, Boolean, Boolean, Boolean>, Integer, Integer>> persistentEffects;


    public StatusEffectStructureRestriction(String structureId, boolean affectAllEntities, Set<EntityType<?>> excludedEntities,
                                            boolean allowAllEffects, Set<StatusEffect> excludedEffects,
                                            Map<StatusEffect, Triplet<Quartet<Boolean, Boolean, Boolean, Boolean>, Integer, Integer>> persistentEffects, RestrictionSource restrictionSource) {

        super(structureId, RestrictionTypes.STATUS_EFFECT, restrictionSource);
        this.affectAllEntities = affectAllEntities;
        this.allowAllEffects = allowAllEffects;
        this.excludedEffects = excludedEffects;
        this.excludedEntities = excludedEntities;
        this.persistentEffects = persistentEffects;
    }

    public boolean shouldHideStatusEffect(Entity entity, StatusEffect effect){
        return this.shouldAffectEntity(entity) && !this.shouldAllowEffect(effect);
    }

    public boolean shouldAffectEntity(Entity entity){
        return entity instanceof LivingEntity && this.shouldAffectAllEntities() != this.getExcludedEntities().contains(entity.getType());
    }

    public boolean shouldAllowEffect(StatusEffect effect){
        return this.shouldAllowAllEffects() != this.getExcludedEffects().contains(effect);
    }

    public Set<StatusEffect> getExcludedEffects() {
        return excludedEffects;
    }

    public boolean shouldAffectAllEntities() {
        return affectAllEntities;
    }

    public boolean shouldAllowAllEffects() {
        return allowAllEffects;
    }

    public Set<EntityType<?>> getExcludedEntities() {
        return excludedEntities;
    }

    public Map<StatusEffect, Triplet<Quartet<Boolean, Boolean, Boolean, Boolean>, Integer, Integer>> getPersistentEffects() {
        return persistentEffects;
    }

}
