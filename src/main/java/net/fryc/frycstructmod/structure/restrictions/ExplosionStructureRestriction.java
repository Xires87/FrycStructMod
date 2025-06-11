package net.fryc.frycstructmod.structure.restrictions;

import net.fryc.frycstructmod.structure.restrictions.registry.RestrictionTypes;
import net.fryc.frycstructmod.structure.restrictions.sources.RestrictionSource;

public class ExplosionStructureRestriction extends AbstractStructureRestriction {
    public ExplosionStructureRestriction(String structureId, RestrictionSource restrictionSource) {
        super(structureId, RestrictionTypes.EXPLOSION, restrictionSource);
    }
}
