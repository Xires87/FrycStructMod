package net.fryc.frycstructmod.mixin.entity;

import net.fryc.frycstructmod.structure.restrictions.AbstractStructureRestriction;
import net.fryc.frycstructmod.structure.restrictions.StatusEffectStructureRestriction;
import net.fryc.frycstructmod.util.RestrictionsHelper;
import net.fryc.frycstructmod.util.ServerRestrictionsHelper;
import net.fryc.frycstructmod.util.interfaces.CanBeAffectedByStructure;
import net.fryc.frycstructmod.util.interfaces.HasRestrictions;
import net.minecraft.entity.Attackable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin extends Entity implements Attackable, CanBeAffectedByStructure {

    private String affectedByStructure = "";

    private final HashMap<StatusEffect, StatusEffectInstance> inactiveStatusEffects = new HashMap<>();

    @Shadow
    private @Final Map<StatusEffect, StatusEffectInstance> activeStatusEffects;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    protected abstract void onStatusEffectRemoved(StatusEffectInstance effect);

    // checks for persistent mobs when persistent mob dies without player's help
    @Inject(method = "onDeath(Lnet/minecraft/entity/damage/DamageSource;)V", at = @At("HEAD"))
    private void onPersistentMobDeath(DamageSource source, CallbackInfo info) {
        if(!this.getWorld().isClient()){
            if(((LivingEntity)(Object)this) instanceof MobEntity mob){
                if(mob.isPersistent() || mob.cannotDespawn()){
                    if(mob.getPrimeAdversary() == null || !mob.getPrimeAdversary().isPlayer()){
                        ServerRestrictionsHelper.executeIfHasStructure(((ServerWorld) mob.getWorld()), mob.getBlockPos(), structure -> {

                            StructureStart start = ((ServerWorld) mob.getWorld()).getStructureAccessor().getStructureAt(this.getBlockPos(), structure);
                            HasRestrictions startWithRestrictions = ((HasRestrictions) (Object) start);

                            if(startWithRestrictions.hasActiveRestrictions()){
                                ServerRestrictionsHelper.checkForPersistentEntitiesFromSource(
                                        startWithRestrictions.getStructureRestrictionInstance(),
                                        ((ServerWorld) mob.getWorld()),
                                        start
                                );
                            }
                        });
                    }
                }
            }
        }
    }

    @Inject(method = "canHaveStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z", at = @At("HEAD"), cancellable = true)
    private void makeEntitiesImmune(StatusEffectInstance effect, CallbackInfoReturnable<Boolean> ret) {
        //this method is also executed on client, so client returns wrong values (nothing bad happened, at least not yet. but it would be better to have it fixed somehow)
        if(!this.getWorld().isClient()){
            Optional<AbstractStructureRestriction> optional = RestrictionsHelper.getRestrictionByTypeIfEntityIsAffectedByStructure("status_effect", ((LivingEntity) (Object) this));
            optional.ifPresent(restriction -> {
                ServerRestrictionsHelper.executeIfHasStructure(((ServerWorld) this.getWorld()), this.getBlockPos(), structure -> {
                    restriction.executeWhenEnabled(((ServerWorld) this.getWorld()), this.getBlockPos(), structure, (start, restrictionInstance) -> {
                        if(restriction instanceof StatusEffectStructureRestriction effectRestriction){
                            if(effectRestriction.shouldHideStatusEffect(this, effect.getEffectType())){
                                if(!effect.getEffectType().isInstant()){
                                    this.addStatusEffectToInactiveEffects(effect);
                                }
                                ret.setReturnValue(false);
                            }
                        }
                    });
                });
            });
        }
    }

    @Inject(method = "tickStatusEffects()V", at = @At("HEAD"))
    private void hideStatusEffects(CallbackInfo info){
        LivingEntity dys = ((LivingEntity) (Object)this);
        if(!this.getWorld().isClient()){
            Optional<AbstractStructureRestriction> optional = RestrictionsHelper.getRestrictionByTypeIfEntityIsAffectedByStructure("status_effect", dys);
            optional.ifPresentOrElse(restriction -> {
                ServerRestrictionsHelper.executeIfHasStructure(((ServerWorld) this.getWorld()), this.getBlockPos(), structure -> {
                    restriction.executeWhenEnabledOrElse(((ServerWorld) this.getWorld()), this.getBlockPos(), structure, (start, restrictionInstance) -> {
                        if(restriction instanceof StatusEffectStructureRestriction effectRestriction){
                            Iterator<Map.Entry<StatusEffect, StatusEffectInstance>> iterator = this.activeStatusEffects.entrySet().iterator();
                            while(iterator.hasNext()){
                                Map.Entry<StatusEffect, StatusEffectInstance> entry = iterator.next();
                                if(effectRestriction.shouldHideStatusEffect(this, entry.getKey())) {
                                    StatusEffectInstance removedEffect = entry.getValue();
                                    this.addStatusEffectToInactiveEffects(removedEffect);
                                    iterator.remove();// TODO networkingiem wysylac info o efektach i wyswietlac nieaktywne efekty (takie przezroczyste bym dal i moze X na ich ikonkach)
                                    this.onStatusEffectRemoved(removedEffect);
                                }
                            }
                        }
                    }, (start, restrictionInstance) -> {
                        this.inactiveStatusEffects.entrySet().removeIf(statusEffectStatusEffectInstanceEntry -> {
                            return dys.addStatusEffect(statusEffectStatusEffectInstanceEntry.getValue());
                        });
                    });
                });
            }, () -> {
                this.inactiveStatusEffects.entrySet().removeIf(statusEffectStatusEffectInstanceEntry -> {
                    return dys.addStatusEffect(statusEffectStatusEffectInstanceEntry.getValue());
                });
            });
        }
    }

    @Inject(method = "writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("TAIL"))
    private void saveInactiveEffectsToNbt(NbtCompound nbt, CallbackInfo info) {
        if (!this.inactiveStatusEffects.isEmpty()) {
            NbtList nbtList = new NbtList();

            for(StatusEffectInstance statusEffectInstance : this.inactiveStatusEffects.values()) {
                nbtList.add(statusEffectInstance.writeNbt(new NbtCompound()));
            }

            nbt.put("InactiveEffects", nbtList);
        }
    }

    @Inject(method = "readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("TAIL"))
    private void readInactiveEffectsFromNbt(NbtCompound nbt, CallbackInfo info) {
        if (nbt.contains("InactiveEffects", 9)) {
            NbtList nbtList = nbt.getList("InactiveEffects", 10);

            for(int i = 0; i < nbtList.size(); ++i) {
                NbtCompound nbtCompound = nbtList.getCompound(i);
                StatusEffectInstance statusEffectInstance = StatusEffectInstance.fromNbt(nbtCompound);
                if (statusEffectInstance != null) {
                    this.inactiveStatusEffects.put(statusEffectInstance.getEffectType(), statusEffectInstance);
                }
            }
        }
    }

    public boolean isAffectedByStructure() {
        return !this.affectedByStructure.isEmpty();
    }

    public void setAffectedByStructure(String affected) {
        this.affectedByStructure = affected;
    }

    public String getStructureId(){
        return this.affectedByStructure;
    }

    private void addStatusEffectToInactiveEffects(StatusEffectInstance effect){
        if(!this.inactiveStatusEffects.containsKey(effect.getEffectType())){
            this.inactiveStatusEffects.put(effect.getEffectType(), effect);
        }
        else {
            this.inactiveStatusEffects.get(effect.getEffectType()).upgrade(effect);
        }
    }

}
