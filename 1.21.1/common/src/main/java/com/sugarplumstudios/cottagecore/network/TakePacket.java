package com.sugarplumstudios.cottagecore.network;

import com.sugarplumstudios.cottagecore.CottagecoreLiving;
import com.sugarplumstudios.cottagecore.collection.CollectionData;
import com.sugarplumstudios.cottagecore.collection.Collectibles;
import com.sugarplumstudios.cottagecore.config.CottageConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Client to server: hand out decorations picked in the collection book. Only for
 * creative players unless the server allows it; anything that isn't one of our
 * collectibles is ignored.
 */
public record TakePacket(List<ResourceLocation> items, boolean stack) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TakePacket> TYPE = new CustomPacketPayload.Type<>(CottagecoreLiving.id("take"));
    public static final StreamCodec<FriendlyByteBuf, TakePacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> encode(packet, buf), TakePacket::decode);
    private static final int MAX_ITEMS = 64;

    @Override
    public Type<TakePacket> type() {
        return TYPE;
    }

    public static void encode(TakePacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.items.size());
        for (ResourceLocation id : packet.items) {
            buf.writeResourceLocation(id);
        }
        buf.writeBoolean(packet.stack);
    }

    public static TakePacket decode(FriendlyByteBuf buf) {
        int size = Math.min(buf.readVarInt(), MAX_ITEMS);
        List<ResourceLocation> items = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            items.add(buf.readResourceLocation());
        }
        return new TakePacket(items, buf.readBoolean());
    }

    /** Runs on the server thread. */
    public static void handle(TakePacket packet, ServerPlayer player) {
        if (player == null) {
            return;
        }
        if (!player.isCreative() && !CottageConfig.COMMON.deliveryInSurvival.get()) {
            player.displayClientMessage(Component.translatable("message.cottagecoreliving.creative_only")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }
        Component last = null;
        int given = 0;
        for (ResourceLocation id : packet.items) {
            if (!CottagecoreLiving.MOD_ID.equals(id.getNamespace())) {
                continue;
            }
            Item item = BuiltInRegistries.ITEM.get(id);
            if (Collectibles.of(item) == null) {
                continue;
            }
            ItemStack stack = new ItemStack(item);
            if (packet.stack) {
                stack.setCount(stack.getMaxStackSize());
            }
            last = stack.getHoverName();
            CollectionData.collect(player, stack, true);
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
            given++;
        }
        if (last != null) {
            Component what = given > 1 ? Component.literal(given + " ✿") : last;
            player.displayClientMessage(Component.translatable("message.cottagecoreliving.delivered", what)
                    .withStyle(ChatFormatting.LIGHT_PURPLE), true);
        }
    }
}
