package com.sugarplumstudios.cottagecore.collection;

import com.sugarplumstudios.cottagecore.catalog.DecorSet;
import com.sugarplumstudios.cottagecore.config.CottageConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Which decorations a player has collected (crafted, picked up, placed or had in
 * their inventory). Saved with the world, keyed by player, so it survives death
 * and works the same in multiplayer on every loader.
 */
public final class CollectionData {
    private CollectionData() {
    }

    @Nullable
    private static CollectionSavedData.PlayerCollection data(Player player) {
        MinecraftServer server = player.level().getServer();
        return server == null ? null : CollectionSavedData.get(server).of(player.getUUID());
    }

    private static void changed(Player player) {
        MinecraftServer server = player.level().getServer();
        if (server != null) {
            CollectionSavedData.get(server).setDirty();
        }
    }

    public static Set<String> collected(Player player) {
        CollectionSavedData.PlayerCollection data = data(player);
        return data == null ? new LinkedHashSet<>() : new LinkedHashSet<>(data.collected());
    }

    public static boolean has(Player player, String id) {
        return collected(player).contains(id);
    }

    /** Records a stack if it is a decoration the player didn't have yet. Returns true if it was new. */
    public static boolean collect(ServerPlayer player, ItemStack stack, boolean announce) {
        if (stack.isEmpty()) {
            return false;
        }
        Collectibles.Entry entry = Collectibles.of(stack.getItem());
        if (entry == null) {
            return false;
        }
        CollectionSavedData.PlayerCollection data = data(player);
        if (data == null || !data.collected().add(entry.id())) {
            return false;
        }
        changed(player);
        if (announce && CottageConfig.COMMON.collectionMessages.get()) {
            player.sendOverlayMessage(Component.translatable("message.cottagecoreliving.collected",
                    stack.getHoverName(), data.collected().size(), Collectibles.total())
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            playTo(player, SoundEvents.AMETHYST_BLOCK_CHIME, 0.6F, 1.4F);
        }
        DecorSet set = entry.set();
        if (announce && set != null && completed(player, set)) {
            player.sendSystemMessage(Component.translatable("message.cottagecoreliving.set_complete", set.displayName())
                    .withStyle(ChatFormatting.GOLD));
            playTo(player, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.6F, 1.0F);
        }
        return true;
    }

    /** A sound only this player hears, at their position. */
    private static void playTo(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
        player.connection.send(new ClientboundSoundPacket(Holder.direct(sound), SoundSource.PLAYERS, player.getX(),
                player.getY(), player.getZ(), volume, pitch, player.getRandom().nextLong()));
    }

    public static boolean completed(Player player, DecorSet set) {
        Set<String> have = collected(player);
        for (Collectibles.Entry entry : Collectibles.inSet(set)) {
            if (!have.contains(entry.id())) {
                return false;
            }
        }
        return true;
    }

    /** Quietly records everything already in the player's inventory. */
    public static void scanInventory(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            collect(player, stack, false);
        }
        collect(player, player.getInventory().getItem(Inventory.SLOT_OFFHAND), false);
    }

    /** True the first time it is asked for a player, then false forever. */
    public static boolean firstBook(Player player) {
        CollectionSavedData.PlayerCollection data = data(player);
        if (data == null || data.receivedBook()) {
            return false;
        }
        data.setReceivedBook(true);
        changed(player);
        return true;
    }

    /**
     * Folds in a collection kept the Forge 1.0 way (in the player's persisted data); used by the Forge loader at
     * login so worlds from 1.0 keep their collections.
     */
    public static void migrate(Player player, Collection<String> ids, boolean receivedBook) {
        CollectionSavedData.PlayerCollection data = data(player);
        if (data == null) {
            return;
        }
        boolean changed = data.collected().addAll(ids);
        if (receivedBook && !data.receivedBook()) {
            data.setReceivedBook(true);
            changed = true;
        }
        if (changed) {
            changed(player);
        }
    }
}
