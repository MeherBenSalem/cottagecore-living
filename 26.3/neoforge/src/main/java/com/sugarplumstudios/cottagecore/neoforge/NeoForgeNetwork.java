package com.sugarplumstudios.cottagecore.neoforge;

import com.sugarplumstudios.cottagecore.client.ClientAccess;
import com.sugarplumstudios.cottagecore.network.OpenBookPacket;
import com.sugarplumstudios.cottagecore.network.TakePacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

final class NeoForgeNetwork {
    private static final String PROTOCOL = "1";

    private NeoForgeNetwork() {
    }

    static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL);
        // Handlers run on the main thread.
        registrar.playToClient(OpenBookPacket.TYPE, OpenBookPacket.STREAM_CODEC, (packet, context) -> ClientAccess.openBook(packet));
        registrar.playToServer(TakePacket.TYPE, TakePacket.STREAM_CODEC, (packet, context) -> {
            if (context.player() instanceof ServerPlayer player) {
                TakePacket.handle(packet, player);
            }
        });
    }
}
