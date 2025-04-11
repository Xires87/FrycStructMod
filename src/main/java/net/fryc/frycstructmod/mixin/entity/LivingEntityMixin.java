package net.fryc.frycstructmod.mixin.entity;

import net.fryc.frycstructmod.FrycStructMod;
import net.fryc.frycstructmod.structure.restrictions.AbstractStructureRestriction;
import net.fryc.frycstructmod.structure.restrictions.DefaultStructureRestriction;
import net.fryc.frycstructmod.structure.restrictions.StatusEffectStructureRestriction;
import net.fryc.frycstructmod.structure.restrictions.StructureRestrictionInstance;
import net.fryc.frycstructmod.util.RestrictionsHelper;
import net.fryc.frycstructmod.util.ServerRestrictionsHelper;
import net.fryc.frycstructmod.util.interfaces.CanBeAffectedByStructure;
import net.fryc.frycstructmod.util.interfaces.HasRestrictions;
import net.minecraft.entity.Attackable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

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
// TODO zamiast dawac odpornosc na efekty, to shadowowac je (beda na szaro, ich czas nie bedzie mijal i nie beda dzialaly dopoki nie wyjdzie sie ze struktury)

    @Inject(method = "canHaveStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z", at = @At("HEAD"), cancellable = true)
    private void makeEntitiesImmune(StatusEffectInstance effect, CallbackInfoReturnable<Boolean> ret) {
        // TODO zrobic ukrywanie efektow i persistent effecty
        //this method is also executed on client, so client returns wrong values (nothing bad happened, at least not yet. but it would be better to have it fixed somehow)
        if(!this.getWorld().isClient()){
            LivingEntity dys = ((LivingEntity) (Object) this);
            Optional<AbstractStructureRestriction> optional = RestrictionsHelper.getRestrictionByTypeIfEntityIsAffectedByStructure("status_effect", dys);
            optional.ifPresent(restriction -> {
                ServerRestrictionsHelper.executeIfHasStructure(((ServerWorld) dys.getWorld()), dys.getBlockPos(), structure -> {
                    Optional<StructureRestrictionInstance> opt = ServerRestrictionsHelper.getStructureRestrictionInstance(
                            ((ServerWorld) dys.getWorld()).getStructureAccessor().getStructureAt(dys.getBlockPos(), structure)
                    );
                    opt.ifPresent(instance -> {
                        if(!instance.isRestrictionDisabled(restriction)){
                            if(restriction instanceof StatusEffectStructureRestriction effectRestriction){
                                if(effectRestriction.shouldIgnoreStatusEffect(this, effect.getEffectType())){
                                    ret.setReturnValue(false);
                                }
                            }
                        }
                    });
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
