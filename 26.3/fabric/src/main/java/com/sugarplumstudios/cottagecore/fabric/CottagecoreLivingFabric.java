package com.sugarplumstudios.cottagecore.fabric;

import com.sugarplumstudios.cottagecore.CottagecoreLiving;
import com.sugarplumstudios.cottagecore.event.CommonEvents;
import com.sugarplumstudios.cottagecore.network.OpenBookPacket;
import com.sugarplumstudios.cottagecore.network.TakePacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class CottagecoreLivingFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CottagecoreLiving.init();

        PayloadTypeRegistry.playS2C().register(OpenBookPacket.TYPE, OpenBookPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(TakePacket.TYPE, TakePacket.STREAM_CODEC);
        // Handlers run on the server's main thread.
        ServerPlayNetworking.registerGlobalReceiver(TakePacket.TYPE, (packet, context) -> TakePacket.handle(packet, context.player()));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> CommonEvents.onLogin(handler.player));
        // Fabric API has no crafted, pickup or place events: new decorations are noticed in the inventory instead.
        ServerTickEvents.END_SERVER_TICK.register(CommonEvents::onServerTick);
    }
}
