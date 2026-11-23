package com.sugarplumstudios.cottagecore.client;

import com.sugarplumstudios.cottagecore.CottagecoreLiving;
import com.sugarplumstudios.cottagecore.registry.ModBlocks;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Fountains and birdbaths borrow vanilla's water texture, tinted like the water of their biome. */
@Mod.EventBusSubscriber(modid = CottagecoreLiving.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private static final int DEFAULT_WATER = 0x3F76E4;

    private ClientModEvents() {
    }

    private static Block[] watery() {
        return ModBlocks.all().stream().filter(b -> b.family().waterTint()).toArray(Block[]::new);
    }

    @SubscribeEvent
    public static void blockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tint) -> level != null && pos != null
                ? BiomeColors.getAverageWaterColor(level, pos) : DEFAULT_WATER, watery());
    }

    @SubscribeEvent
    public static void itemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tint) -> tint == 0 ? DEFAULT_WATER : -1, watery());
    }
}
