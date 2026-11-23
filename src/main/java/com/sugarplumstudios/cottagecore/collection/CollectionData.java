package com.sugarplumstudios.cottagecore.collection;

import com.sugarplumstudios.cottagecore.catalog.DecorSet;
import com.sugarplumstudios.cottagecore.config.CottageConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Which decorations a player has collected (crafted, picked up, placed or had in
 * their inventory). Stored in the player's persisted data, so it survives death
 * and is saved with the player on the server: works the same in multiplayer.
 */
public final class CollectionData {
    private static final String KEY = "cottagecoreliving_collected";
    private static final String RECEIVED_BOOK = "cottagecoreliving_received_book";

    private CollectionData() {
    }

    private static CompoundTag persisted(Player player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
    }

    private static void save(Player player, CompoundTag persisted) {
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }

    public static Set<String> collected(Player player) {
        ListTag list = persisted(player).getList(KEY, Tag.TAG_STRING);
        Set<String> out = new LinkedHashSet<>();
        for (int i = 0; i < list.size(); i++) {
            out.add(list.getString(i));
        }
        return out;
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
        CompoundTag persisted = persisted(player);
        ListTag list = persisted.getList(KEY, Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            if (list.getString(i).equals(entry.id())) {
                return false;
            }
        }
        list.add(StringTag.valueOf(entry.id()));
        persisted.put(KEY, list);
        save(player, persisted);
        if (announce && CottageConfig.COMMON.collectionMessages.get()) {
            player.displayClientMessage(Component.translatable("message.cottagecoreliving.collected",
                    stack.getItem().getDescription(), list.size(), Collectibles.total())
                    .withStyle(ChatFormatting.LIGHT_PURPLE), true);
            player.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6F, 1.4F);
        }
        DecorSet set = entry.set();
        if (announce && set != null && completed(player, set)) {
            player.sendSystemMessage(Component.translatable("message.cottagecoreliving.set_complete", set.displayName())
                    .withStyle(ChatFormatting.GOLD));
            player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 0.6F, 1.0F);
        }
        return true;
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
        for (ItemStack stack : player.getInventory().items) {
            collect(player, stack, false);
        }
        for (ItemStack stack : player.getInventory().offhand) {
            collect(player, stack, false);
        }
    }

    /** True the first time it is asked for a player, then false forever. */
    public static boolean firstBook(Player player) {
        CompoundTag persisted = persisted(player);
        if (persisted.getBoolean(RECEIVED_BOOK)) {
            return false;
        }
        persisted.putBoolean(RECEIVED_BOOK, true);
        save(player, persisted);
        return true;
    }
}
