package net.fryc.frycstructmod.mixin.entity;

import net.fryc.frycstructmod.structure.restrictions.AbstractStructureRestriction;
import net.fryc.frycstructmod.structure.restrictions.StatusEffectStructureRestriction;
import net.fryc.frycstructmod.util.RestrictionsHelper;
import net.fryc.frycstructmod.util.interfaces.CanBeAffectedByStructure;
import net.fryc.frycstructmod.util.interfaces.HasRestrictions;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin extends Entity implements Attackable, CanBeAffectedByStructure {

    private String affectedByStructure = "";

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    // checks for persistent mobs when persistent mob dies without player's help
    @Inject(method = "onDeath(Lnet/minecraft/entity/damage/DamageSource;)V", at = @At("HEAD"))
    private void onPersistentMobDeath(DamageSource source, CallbackInfo info) {
        if(!this.getWorld().isClient()){
            if(((LivingEntity)(Object)this) instanceof MobEntity mob){
                if(mob.isPersistent() || mob.cannotDespawn()){
                    if(mob.getPrimeAdversary() == null || !mob.getPrimeAdversary().isPlayer()){
                        RestrictionsHelper.executeIfHasStructure(((ServerWorld) mob.getWorld()), mob.getBlockPos(), structure -> {

                            StructureStart start = ((ServerWorld) mob.getWorld()).getStructureAccessor().getStructureAt(this.getBlockPos(), structure);
                            HasRestrictions startWithRestrictions = ((HasRestrictions) (Object) start);

                            if(startWithRestrictions.hasActiveRestrictions()){
                                RestrictionsHelper.checkForPersistentEntitiesFromSource(
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
// TODO zamiast dawac odpornosc na efekty, to shadowowac je (beda na szaro, ich czas nie bedzie mijal i nie beda dzialaly dopoki nie wyjdzie sie ze struktury)

    @Inject(method = "canHaveStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z", at = @At("HEAD"), cancellable = true)
    private void makeEntitiesImmune(StatusEffectInstance effect, CallbackInfoReturnable<Boolean> ret) {
        if(!this.getWorld().isClient()){
            RestrictionsHelper.executeIfHasStructure(((ServerWorld) this.getWorld()), this.getBlockPos(), structure -> {
                Optional<AbstractStructureRestriction> optional = RestrictionsHelper.getRestrictionByType(
                        "status_effect", this.getWorld().getRegistryManager().get(RegistryKeys.STRUCTURE).getId(structure)
                );
                // TODO to zawsze bedzie dzialalo bo zle jest zrobiona odpornosc na restrykcje ( i nie sprawdzam tu instancji tylko czy jestem w wiosce)
                optional.ifPresent(restriction -> {
                    if(restriction instanceof StatusEffectStructureRestriction effectRestriction){
                        if(effectRestriction.shouldMakeEntityImmune(this, effect.getEffectType())){
                            ret.setReturnValue(false);// TODO podmienic to na shadowowanie efektow
                        }
                    }
                });
            });
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

}
