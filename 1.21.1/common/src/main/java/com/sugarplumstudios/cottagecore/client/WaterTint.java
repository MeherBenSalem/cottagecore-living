package com.sugarplumstudios.cottagecore.client;

import com.sugarplumstudios.cottagecore.registry.ModBlocks;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

/** Fountains and birdbaths borrow vanilla's water texture, tinted like the water of their biome. */
public final class WaterTint {
    public static final int DEFAULT_WATER = 0x3F76E4;

    private WaterTint() {
    }

    public static Block[] watery() {
        return ModBlocks.all().stream().filter(b -> b.family().waterTint()).toArray(Block[]::new);
    }

    public static int block(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos) {
        return level != null && pos != null ? BiomeColors.getAverageWaterColor(level, pos) : DEFAULT_WATER;
    }

    public static int item(int tint) {
        return tint == 0 ? DEFAULT_WATER : -1;
    }
}
