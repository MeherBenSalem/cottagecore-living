package com.sugarplumstudios.cottagecore;

import com.mojang.logging.LogUtils;
import com.sugarplumstudios.cottagecore.config.CottageConfig;
import com.sugarplumstudios.cottagecore.registry.ModBlockEntities;
import com.sugarplumstudios.cottagecore.registry.ModBlocks;
import com.sugarplumstudios.cottagecore.registry.ModCreativeTabs;
import com.sugarplumstudios.cottagecore.registry.ModItems;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

/**
 * Cottagecore Living, a Sugarplum Studios mod: hundreds of small decorations that
 * make interiors and gardens feel lived-in. Loader-independent entry point: each
 * loader calls {@link #init()} from its own mod constructor or initializer.
 */
public final class CottagecoreLiving {
    public static final String MOD_ID = "cottagecoreliving";
    public static final Logger LOGGER = LogUtils.getLogger();

    private CottagecoreLiving() {
    }

    /** Reads the config and registers every block, item, block entity and creative tab. */
    public static void init() {
        CottageConfig.loadCommon();
        ModBlocks.init();
        ModItems.init();
        ModBlockEntities.init();
        ModCreativeTabs.init();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
