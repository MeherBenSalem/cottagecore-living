package com.sugarplumstudios.cottagecore.forge;

import com.sugarplumstudios.cottagecore.CottagecoreLiving;
import com.sugarplumstudios.cottagecore.client.ClientAccess;
import com.sugarplumstudios.cottagecore.network.OpenBookPacket;
import com.sugarplumstudios.cottagecore.network.TakePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/** The mod's Forge channel, unchanged from 1.0 (cottagecoreliving:main, protocol 1). */
final class ForgeNetwork {
    private static final String PROTOCOL = "1";
    static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            CottagecoreLiving.id("main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private ForgeNetwork() {
    }

    static void register() {
        int id = 0;
        CHANNEL.messageBuilder(OpenBookPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenBookPacket::encode)
                .decoder(OpenBookPacket::decode)
                .consumerMainThread((packet, context) -> {
                    context.get().setPacketHandled(true);
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientAccess.openBook(packet));
                })
                .add();
        CHANNEL.messageBuilder(TakePacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(TakePacket::encode)
                .decoder(TakePacket::decode)
                .consumerMainThread((packet, context) -> {
                    ServerPlayer player = context.get().getSender();
                    context.get().setPacketHandled(true);
                    TakePacket.handle(packet, player);
                })
                .add();
    }

    static void sendToServer(TakePacket packet) {
        CHANNEL.sendToServer(packet);
    }

    static void sendTo(ServerPlayer player, OpenBookPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
