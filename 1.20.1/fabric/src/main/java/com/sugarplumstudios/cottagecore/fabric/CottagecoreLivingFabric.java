package com.sugarplumstudios.cottagecore.fabric;

import com.sugarplumstudios.cottagecore.CottagecoreLiving;
import com.sugarplumstudios.cottagecore.event.CommonEvents;
import com.sugarplumstudios.cottagecore.network.TakePacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class CottagecoreLivingFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CottagecoreLiving.init();

        ServerPlayNetworking.registerGlobalReceiver(TakePacket.ID, (server, player, handler, buf, sender) -> {
            TakePacket packet = TakePacket.decode(buf);
            server.execute(() -> TakePacket.handle(packet, player));
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> CommonEvents.onLogin(handler.player));
        // Fabric API has no crafted, pickup or place events: new decorations are noticed in the inventory instead.
        ServerTickEvents.END_SERVER_TICK.register(CommonEvents::onServerTick);
    }
}
