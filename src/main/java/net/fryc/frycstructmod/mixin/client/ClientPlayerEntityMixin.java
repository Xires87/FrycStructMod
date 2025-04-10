package net.fryc.frycstructmod.mixin.client;

import net.fryc.frycstructmod.structure.restrictions.StructureRestrictionInstance;
import net.fryc.frycstructmod.util.interfaces.client.HoldsStructureRestrictionInstance;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientPlayerEntity.class)
abstract class ClientPlayerEntityMixin implements HoldsStructureRestrictionInstance {

    StructureRestrictionInstance instance = null;

    @Override
    public @Nullable StructureRestrictionInstance getStructureRestrictionInstance() {
        return this.instance;
    }

    @Override
    public void setStructureRestrictionInstance(StructureRestrictionInstance instance) {
        this.instance = instance;
    }
}
