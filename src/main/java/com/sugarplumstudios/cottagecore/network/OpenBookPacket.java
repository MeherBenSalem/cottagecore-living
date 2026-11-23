package com.sugarplumstudios.cottagecore.network;

import com.sugarplumstudios.cottagecore.catalog.Season;
import com.sugarplumstudios.cottagecore.client.ClientAccess;
import com.sugarplumstudios.cottagecore.collection.CollectionData;
import com.sugarplumstudios.cottagecore.config.CottageConfig;
import com.sugarplumstudios.cottagecore.season.SeasonManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Server to client: open the collection book with this player's collection and the world's season. */
public record OpenBookPacket(List<String> collected, Season season, CottageConfig.SeasonMode mode, boolean canTake) {
    private static final int MAX = 4096;

    public static OpenBookPacket of(ServerPlayer player) {
        boolean canTake = player.isCreative() || CottageConfig.COMMON.deliveryInSurvival.get();
        return new OpenBookPacket(List.copyOf(CollectionData.collected(player)), SeasonManager.current(player.level()),
                CottageConfig.COMMON.seasonMode.get(), canTake);
    }

    public static void encode(OpenBookPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.collected.size());
        for (String id : packet.collected) {
            buf.writeUtf(id, 128);
        }
        buf.writeEnum(packet.season);
        buf.writeEnum(packet.mode);
        buf.writeBoolean(packet.canTake);
    }

    public static OpenBookPacket decode(FriendlyByteBuf buf) {
        int size = Math.min(buf.readVarInt(), MAX);
        List<String> ids = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ids.add(buf.readUtf(128));
        }
        return new OpenBookPacket(ids, buf.readEnum(Season.class), buf.readEnum(CottageConfig.SeasonMode.class),
                buf.readBoolean());
    }

    public static void handle(OpenBookPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().setPacketHandled(true);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientAccess.openBook(packet));
    }
}
