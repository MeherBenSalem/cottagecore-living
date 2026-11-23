package com.sugarplumstudios.cottagecore.network;

import com.sugarplumstudios.cottagecore.CottagecoreLiving;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            CottagecoreLiving.id("main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private ModNetwork() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(OpenBookPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenBookPacket::encode)
                .decoder(OpenBookPacket::decode)
                .consumerMainThread(OpenBookPacket::handle)
                .add();
        CHANNEL.messageBuilder(TakePacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(TakePacket::encode)
                .decoder(TakePacket::decode)
                .consumerMainThread(TakePacket::handle)
                .add();
    }

    public static void sendToServer(Object message) {
        CHANNEL.sendToServer(message);
    }

    public static void sendTo(ServerPlayer player, Object message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
