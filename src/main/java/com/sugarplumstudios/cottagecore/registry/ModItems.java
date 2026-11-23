package com.sugarplumstudios.cottagecore.registry;

import com.sugarplumstudios.cottagecore.CottagecoreLiving;
import com.sugarplumstudios.cottagecore.catalog.DecorSet;
import com.sugarplumstudios.cottagecore.item.CollectionBookItem;
import com.sugarplumstudios.cottagecore.item.DecorBlockItem;
import com.sugarplumstudios.cottagecore.item.TableclothItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CottagecoreLiving.MOD_ID);

    public static final Map<String, RegistryObject<Item>> DECORATIONS = new LinkedHashMap<>();
    public static final Map<DecorSet, RegistryObject<Item>> TABLECLOTHS = new EnumMap<>(DecorSet.class);

    public static final RegistryObject<Item> COLLECTION_BOOK = ITEMS.register("collection_book",
            () -> new CollectionBookItem(new Item.Properties().stacksTo(1)));

    static {
        for (Map.Entry<String, RegistryObject<Block>> entry : ModBlocks.byId().entrySet()) {
            RegistryObject<Block> block = entry.getValue();
            DECORATIONS.put(entry.getKey(), ITEMS.register(entry.getKey(), () -> new DecorBlockItem(block.get(), new Item.Properties())));
        }
        for (DecorSet set : DecorSet.values()) {
            TABLECLOTHS.put(set, ITEMS.register(set.id() + "_tablecloth", () -> new TableclothItem(set, new Item.Properties())));
        }
    }

    private ModItems() {
    }

    public static Item tablecloth(DecorSet set) {
        return TABLECLOTHS.get(set).get();
    }

    public static Item decoration(String id) {
        return DECORATIONS.get(id).get();
    }
}
