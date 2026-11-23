package com.sugarplumstudios.cottagecore.collection;

import com.sugarplumstudios.cottagecore.block.DecorBlock;
import com.sugarplumstudios.cottagecore.catalog.Category;
import com.sugarplumstudios.cottagecore.catalog.DecorCatalog;
import com.sugarplumstudios.cottagecore.catalog.DecorFamily;
import com.sugarplumstudios.cottagecore.catalog.DecorSet;
import com.sugarplumstudios.cottagecore.catalog.DecorVariant;
import com.sugarplumstudios.cottagecore.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Everything that counts towards a player's collection, in book order: every
 * decoration plus the five tablecloths (listed right after the tables).
 */
public final class Collectibles {
    private static List<Entry> all;
    private static Map<Item, Entry> byItem;

    private Collectibles() {
    }

    /** One collectible. {@code family} is null for tablecloths. */
    public record Entry(String id, Item item, Category category, String group, @Nullable DecorSet set,
                        @Nullable DecorFamily family) {
    }

    public static synchronized List<Entry> all() {
        if (all == null) {
            List<Entry> out = new ArrayList<>();
            for (DecorFamily family : DecorCatalog.FAMILIES) {
                for (DecorVariant variant : family.variants()) {
                    out.add(new Entry(variant.id(), ModItems.decoration(variant.id()), family.category(), family.group(),
                            variant.set(), family));
                }
                if (family.id().equals("cottage_table")) {
                    for (DecorSet set : DecorSet.values()) {
                        out.add(new Entry(set.id() + "_tablecloth", ModItems.tablecloth(set), Category.INTERIOR,
                                family.group(), set, null));
                    }
                }
            }
            Map<Item, Entry> index = new IdentityHashMap<>();
            for (Entry entry : out) {
                index.put(entry.item(), entry);
            }
            byItem = index;
            all = Collections.unmodifiableList(out);
        }
        return all;
    }

    @Nullable
    public static Entry of(Item item) {
        all();
        return byItem.get(item);
    }

    @Nullable
    public static Entry byId(String id) {
        for (Entry entry : all()) {
            if (entry.id().equals(id)) {
                return entry;
            }
        }
        return null;
    }

    public static int total() {
        return all().size();
    }

    public static List<Entry> inSet(DecorSet set) {
        return all().stream().filter(e -> e.set() == set).toList();
    }

    public static List<Entry> inCategory(Category category) {
        return all().stream().filter(e -> e.category() == category).toList();
    }

    /** The registry path of an item, which is also its collection id. */
    public static String idOf(Item item) {
        var key = BuiltInRegistries.ITEM.getKey(item);
        return key == null ? "" : key.getPath();
    }

    @Nullable
    public static DecorFamily familyOf(Item item) {
        if (item instanceof net.minecraft.world.item.BlockItem blockItem && blockItem.getBlock() instanceof DecorBlock decor) {
            return decor.family();
        }
        return null;
    }
}
