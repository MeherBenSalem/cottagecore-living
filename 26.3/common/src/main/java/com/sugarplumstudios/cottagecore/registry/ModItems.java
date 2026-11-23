package com.sugarplumstudios.cottagecore.registry;

import com.sugarplumstudios.cottagecore.catalog.DecorSet;
import com.sugarplumstudios.cottagecore.item.CollectionBookItem;
import com.sugarplumstudios.cottagecore.item.DecorBlockItem;
import com.sugarplumstudios.cottagecore.item.TableclothItem;
import com.sugarplumstudios.cottagecore.platform.RegistryEntry;
import com.sugarplumstudios.cottagecore.platform.Services;
import com.sugarplumstudios.cottagecore.CottagecoreLiving;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ModItems {
    public static final Map<String, RegistryEntry<Item>> DECORATIONS = new LinkedHashMap<>();
    public static final Map<DecorSet, RegistryEntry<Item>> TABLECLOTHS = new EnumMap<>(DecorSet.class);

    public static RegistryEntry<Item> COLLECTION_BOOK;

    private ModItems() {
    }

    public static void init() {
        COLLECTION_BOOK = Services.PLATFORM.register(Registries.ITEM, "collection_book",
                () -> new CollectionBookItem(props("collection_book").stacksTo(1)));
        for (Map.Entry<String, RegistryEntry<Block>> entry : ModBlocks.byId().entrySet()) {
            RegistryEntry<Block> block = entry.getValue();
            DECORATIONS.put(entry.getKey(), Services.PLATFORM.register(Registries.ITEM, entry.getKey(),
                    () -> new DecorBlockItem(block.get(), props(entry.getKey()).useBlockDescriptionPrefix())));
        }
        for (DecorSet set : DecorSet.values()) {
            TABLECLOTHS.put(set, Services.PLATFORM.register(Registries.ITEM, set.id() + "_tablecloth",
                    () -> new TableclothItem(set, props(set.id() + "_tablecloth"))));
        }
    }

    /** Item properties carrying the item's registry id (required since 1.21.2). */
    private static Item.Properties props(String name) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, CottagecoreLiving.id(name)));
    }

    public static Item tablecloth(DecorSet set) {
        return TABLECLOTHS.get(set).get();
    }

    public static Item decoration(String id) {
        return DECORATIONS.get(id).get();
    }
}
