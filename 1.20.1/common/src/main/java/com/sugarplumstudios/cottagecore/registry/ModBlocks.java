package com.sugarplumstudios.cottagecore.registry;

import com.sugarplumstudios.cottagecore.block.DecorBlock;
import com.sugarplumstudios.cottagecore.block.StorageDecorBlock;
import com.sugarplumstudios.cottagecore.catalog.DecorCatalog;
import com.sugarplumstudios.cottagecore.catalog.DecorFamily;
import com.sugarplumstudios.cottagecore.catalog.DecorVariant;
import com.sugarplumstudios.cottagecore.platform.RegistryEntry;
import com.sugarplumstudios.cottagecore.platform.Services;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Registers one block per decoration variant in the generated catalog, in catalog order. */
public final class ModBlocks {
    private static final Map<String, RegistryEntry<Block>> BY_ID = new LinkedHashMap<>();

    private ModBlocks() {
    }

    public static void init() {
        for (DecorFamily family : DecorCatalog.FAMILIES) {
            for (DecorVariant variant : family.variants()) {
                BY_ID.put(variant.id(), Services.PLATFORM.register(Registries.BLOCK, variant.id(),
                        () -> family.kind().create(family, variant)));
            }
        }
    }

    public static Map<String, RegistryEntry<Block>> byId() {
        return Collections.unmodifiableMap(BY_ID);
    }

    public static Block get(String id) {
        RegistryEntry<Block> block = BY_ID.get(id);
        if (block == null) {
            throw new IllegalArgumentException("Unknown decoration " + id);
        }
        return block.get();
    }

    /** Every decoration block, in catalog order. */
    public static List<DecorBlock> all() {
        List<DecorBlock> out = new ArrayList<>(BY_ID.size());
        for (RegistryEntry<Block> block : BY_ID.values()) {
            out.add((DecorBlock) block.get());
        }
        return out;
    }

    public static Block[] storageBlocks() {
        return BY_ID.values().stream().map(RegistryEntry::get).filter(b -> b instanceof StorageDecorBlock)
                .toArray(Block[]::new);
    }
}
