package com.sugarplumstudios.cottagecore.collection;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Every player's decoration collection, saved with the world in {@code data/cottagecoreliving_collection.dat}. */
public class CollectionSavedData extends SavedData {
    private static final String NAME = "cottagecoreliving_collection";
    private final Map<UUID, PlayerCollection> players = new HashMap<>();

    /** One player's collected decoration ids (in the order collected) and whether they were given the book. */
    public static final class PlayerCollection {
        private final Set<String> collected = new LinkedHashSet<>();
        private boolean receivedBook;

        public Set<String> collected() {
            return collected;
        }

        public boolean receivedBook() {
            return receivedBook;
        }

        public void setReceivedBook(boolean receivedBook) {
            this.receivedBook = receivedBook;
        }
    }

    public static CollectionSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(CollectionSavedData::load, CollectionSavedData::new, NAME);
    }

    public PlayerCollection of(UUID player) {
        return players.computeIfAbsent(player, id -> new PlayerCollection());
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        players.forEach((id, player) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Player", id);
            ListTag ids = new ListTag();
            for (String collected : player.collected) {
                ids.add(StringTag.valueOf(collected));
            }
            entry.put("Collected", ids);
            entry.putBoolean("ReceivedBook", player.receivedBook);
            list.add(entry);
        });
        tag.put("Players", list);
        return tag;
    }

    public static CollectionSavedData load(CompoundTag tag) {
        CollectionSavedData data = new CollectionSavedData();
        ListTag list = tag.getList("Players", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (!entry.hasUUID("Player")) {
                continue;
            }
            PlayerCollection player = data.of(entry.getUUID("Player"));
            ListTag ids = entry.getList("Collected", Tag.TAG_STRING);
            for (int j = 0; j < ids.size(); j++) {
                player.collected.add(ids.getString(j));
            }
            player.receivedBook = entry.getBoolean("ReceivedBook");
        }
        return data;
    }
}
