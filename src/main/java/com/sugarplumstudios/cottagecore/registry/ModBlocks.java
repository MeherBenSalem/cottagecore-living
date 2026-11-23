package com.sugarplumstudios.cottagecore.registry;

import com.sugarplumstudios.cottagecore.CottagecoreLiving;
import com.sugarplumstudios.cottagecore.block.DecorBlock;
import com.sugarplumstudios.cottagecore.block.StorageDecorBlock;
import com.sugarplumstudios.cottagecore.catalog.DecorCatalog;
import com.sugarplumstudios.cottagecore.catalog.DecorFamily;
import com.sugarplumstudios.cottagecore.catalog.DecorVariant;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Registers one block per decoration variant in the generated catalog, in catalog order. */
public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CottagecoreLiving.MOD_ID);

    private static final Map<String, RegistryObject<Block>> BY_ID = new LinkedHashMap<>();

    static {
        for (DecorFamily family : DecorCatalog.FAMILIES) {
            for (DecorVariant variant : family.variants()) {
                BY_ID.put(variant.id(), BLOCKS.register(variant.id(), () -> family.kind().create(family, variant)));
            }
        }
    }

    private ModBlocks() {
    }

    public static Map<String, RegistryObject<Block>> byId() {
        return Collections.unmodifiableMap(BY_ID);
    }

    public static Block get(String id) {
        RegistryObject<Block> block = BY_ID.get(id);
        if (block == null) {
            throw new IllegalArgumentException("Unknown decoration " + id);
        }
        return block.get();
    }

    /** Every decoration block, in catalog order. */
    public static List<DecorBlock> all() {
        List<DecorBlock> out = new ArrayList<>(BY_ID.size());
        for (RegistryObject<Block> block : BY_ID.values()) {
            out.add((DecorBlock) block.get());
        }
        return out;
    }

    public static Block[] storageBlocks() {
        return BY_ID.values().stream().map(RegistryObject::get).filter(b -> b instanceof StorageDecorBlock)
                .toArray(Block[]::new);
    }
}
