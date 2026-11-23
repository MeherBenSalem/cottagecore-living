package com.sugarplumstudios.cottagecore.client;

import com.sugarplumstudios.cottagecore.registry.ModBlocks;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Fountains and birdbaths borrow vanilla's water texture, tinted like the water of their biome. Their items are
 * tinted by the {@code tints} entry of their item model definitions.
 */
public final class WaterTint implements BlockTintSource {
    public static final int DEFAULT_WATER = 0x3F76E4;
    public static final WaterTint INSTANCE = new WaterTint();

    private WaterTint() {
    }

    public static Block[] watery() {
        return ModBlocks.all().stream().filter(b -> b.family().waterTint()).toArray(Block[]::new);
    }

    @Override
    public int color(BlockState state) {
        return ARGB.opaque(DEFAULT_WATER);
    }

    @Override
    public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        return ARGB.opaque(BiomeColors.getAverageWaterColor(level, pos));
    }
}
