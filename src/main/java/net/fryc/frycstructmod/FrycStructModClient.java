package net.fryc.frycstructmod;

import net.fabricmc.api.ClientModInitializer;
import net.fryc.frycstructmod.network.ModPackets;

public class FrycStructModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModPackets.registerS2CPackets();
    }
}
