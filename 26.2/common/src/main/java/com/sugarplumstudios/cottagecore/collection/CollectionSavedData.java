package com.sugarplumstudios.cottagecore.collection;

import com.sugarplumstudios.cottagecore.CottagecoreLiving;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Every player's decoration collection, saved with the world. */
public class CollectionSavedData extends SavedData {
    public static final SavedDataType<CollectionSavedData> TYPE = new SavedDataType<>(CottagecoreLiving.id("collection"),
            CollectionSavedData::new, CompoundTag.CODEC.xmap(CollectionSavedData::load, data -> data.save(new CompoundTag())),
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

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
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public PlayerCollection of(UUID player) {
        return players.computeIfAbsent(player, id -> new PlayerCollection());
    }

    CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        players.forEach((id, player) -> {
            CompoundTag entry = new CompoundTag();
            entry.store("Player", UUIDUtil.CODEC, id);
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

    static CollectionSavedData load(CompoundTag tag) {
        CollectionSavedData data = new CollectionSavedData();
        for (Tag t : tag.getListOrEmpty("Players")) {
            if (!(t instanceof CompoundTag entry)) {
                continue;
            }
            Optional<UUID> id = entry.read("Player", UUIDUtil.CODEC);
            if (id.isEmpty()) {
                continue;
            }
            PlayerCollection player = data.of(id.get());
            for (Tag collected : entry.getListOrEmpty("Collected")) {
                collected.asString().ifPresent(player.collected::add);
            }
            player.receivedBook = entry.getBooleanOr("ReceivedBook", false);
        }
        return data;
    }
}
