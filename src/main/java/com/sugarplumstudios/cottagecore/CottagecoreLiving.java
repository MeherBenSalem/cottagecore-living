package com.sugarplumstudios.cottagecore;

import com.mojang.logging.LogUtils;
import com.sugarplumstudios.cottagecore.config.CottageConfig;
import com.sugarplumstudios.cottagecore.network.ModNetwork;
import com.sugarplumstudios.cottagecore.registry.ModBlockEntities;
import com.sugarplumstudios.cottagecore.registry.ModBlocks;
import com.sugarplumstudios.cottagecore.registry.ModCreativeTabs;
import com.sugarplumstudios.cottagecore.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Cottagecore Living, a Sugarplum Studios mod: hundreds of small decorations that
 * make interiors and gardens feel lived-in.
 */
@Mod(CottagecoreLiving.MOD_ID)
public class CottagecoreLiving {
    public static final String MOD_ID = "cottagecoreliving";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CottagecoreLiving() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModCreativeTabs.TABS.register(modBus);

        ModNetwork.register();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CottageConfig.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CottageConfig.CLIENT_SPEC);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
