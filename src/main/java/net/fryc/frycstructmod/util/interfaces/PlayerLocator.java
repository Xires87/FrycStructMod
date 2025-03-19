package net.fryc.frycstructmod.util.interfaces;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

import java.util.List;

public interface PlayerLocator {

    List<PlayerEntity> getPlayersInStructure(World world);
}
