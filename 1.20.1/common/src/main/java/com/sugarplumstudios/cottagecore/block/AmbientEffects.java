package com.sugarplumstudios.cottagecore.block;

import com.sugarplumstudios.cottagecore.catalog.Ambient;
import com.sugarplumstudios.cottagecore.catalog.DecorFamily;
import com.sugarplumstudios.cottagecore.catalog.LightMode;
import com.sugarplumstudios.cottagecore.config.CottageConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Client-side ambience, called from {@code animateTick} (which only runs on the
 * client): candle flames, fireflies, fountain splashes, chimes in the breeze,
 * steam, petals and sparkles. Purely cosmetic and gated by the client config.
 */
public final class AmbientEffects {
    private AmbientEffects() {
    }

    static void animate(DecorBlock block, BlockState state, Level level, BlockPos pos, RandomSource random) {
        DecorFamily family = block.family();
        if (family.ambient() == Ambient.NONE && family.lightMode() != LightMode.CANDLE) {
            return;
        }
        boolean particles = CottageConfig.CLIENT.ambientParticles.get();
        boolean sounds = CottageConfig.CLIENT.ambientSounds.get();
        if (particles && family.lightMode() == LightMode.CANDLE && state.getValue(DecorBlock.LIT)) {
            for (Vec3 flame : block.flamePositions(state, pos)) {
                if (random.nextFloat() < 0.3F) {
                    level.addParticle(ParticleTypes.SMALL_FLAME, flame.x, flame.y, flame.z, 0.0D, 0.0D, 0.0D);
                }
                if (random.nextFloat() < 0.04F) {
                    level.addParticle(ParticleTypes.SMOKE, flame.x, flame.y + 0.1D, flame.z, 0.0D, 0.0D, 0.0D);
                }
            }
        }
        AABB box = block.getShape(state, level, pos, net.minecraft.world.phys.shapes.CollisionContext.empty()).bounds();
        double x = pos.getX() + box.minX + random.nextDouble() * (box.maxX - box.minX);
        double z = pos.getZ() + box.minZ + random.nextDouble() * (box.maxZ - box.minZ);
        double top = pos.getY() + box.maxY;
        switch (family.ambient()) {
            case FIREFLIES -> {
                if (particles && random.nextInt(3) == 0) {
                    level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 1.2D,
                            pos.getY() + 0.2D + random.nextDouble() * 0.8D, pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 1.2D,
                            (random.nextDouble() - 0.5D) * 0.01D, 0.004D, (random.nextDouble() - 0.5D) * 0.01D);
                }
            }
            case FOUNTAIN -> {
                if (particles) {
                    level.addParticle(ParticleTypes.SPLASH, pos.getX() + 0.5D, pos.getY() + 0.8D, pos.getZ() + 0.5D,
                            (random.nextDouble() - 0.5D) * 0.2D, 0.1D, (random.nextDouble() - 0.5D) * 0.2D);
                    if (random.nextInt(3) == 0) {
                        level.addParticle(ParticleTypes.FALLING_WATER, pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.4D,
                                pos.getY() + 0.62D, pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.4D, 0.0D, 0.0D, 0.0D);
                    }
                }
                if (sounds && random.nextInt(60) == 0) {
                    level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, SoundEvents.WATER_AMBIENT,
                            SoundSource.BLOCKS, 0.15F, 1.2F + random.nextFloat() * 0.3F, false);
                }
            }
            case CHIMES -> {
                int chance = level.isRaining() ? 12 : 45;
                if (sounds && random.nextInt(chance) == 0) {
                    level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                            SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.35F, 0.8F + random.nextFloat() * 0.9F, false);
                }
            }
            case STEAM -> {
                if (particles && random.nextInt(4) == 0) {
                    level.addParticle(ParticleTypes.CLOUD, x, top + 0.05D, z, 0.0D, 0.015D, 0.0D);
                }
            }
            case PETALS -> {
                if (particles && random.nextInt(6) == 0) {
                    level.addParticle(ParticleTypes.CHERRY_LEAVES, x, pos.getY() + box.minY + random.nextDouble()
                            * (box.maxY - box.minY), z, 0.0D, 0.0D, 0.0D);
                }
            }
            case SPARKLES -> {
                if (particles && random.nextInt(5) == 0) {
                    level.addParticle(ParticleTypes.HAPPY_VILLAGER, x, pos.getY() + random.nextDouble() * 0.7D, z,
                            0.0D, 0.0D, 0.0D);
                }
            }
            case BIRDS -> {
                if (sounds && level.isDay() && random.nextInt(180) == 0) {
                    level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, SoundEvents.PARROT_AMBIENT,
                            SoundSource.AMBIENT, 0.25F, 1.6F + random.nextFloat() * 0.4F, false);
                    if (particles) {
                        level.addParticle(ParticleTypes.NOTE, pos.getX() + 0.5D, top + 0.2D, pos.getZ() + 0.5D,
                                random.nextDouble(), 0.0D, 0.0D);
                    }
                }
            }
            default -> {
            }
        }
    }

    /** Leaves scatter as something walks through a leaf pile. */
    static void rustle(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity.getDeltaMovement().horizontalDistanceSqr() < 0.0004D) {
            return;
        }
        RandomSource random = level.random;
        if (CottageConfig.CLIENT.ambientParticles.get() && random.nextInt(3) == 0) {
            level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), entity.getX(), pos.getY() + 0.2D,
                    entity.getZ(), (random.nextDouble() - 0.5D) * 0.2D, 0.15D, (random.nextDouble() - 0.5D) * 0.2D);
        }
        if (CottageConfig.CLIENT.ambientSounds.get() && random.nextInt(12) == 0) {
            level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.AZALEA_LEAVES_STEP,
                    SoundSource.BLOCKS, 0.4F, 0.9F + random.nextFloat() * 0.3F, false);
        }
    }
}
