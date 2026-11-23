package com.sugarplumstudios.cottagecore.block;

import com.sugarplumstudios.cottagecore.catalog.DecorFamily;
import com.sugarplumstudios.cottagecore.catalog.Interaction;
import com.sugarplumstudios.cottagecore.config.CottageConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * Right-click behaviour shared by many decorations. Everything that changes the
 * world or talks to the player happens on the server; the client just reports
 * success so the arm swings.
 */
public final class Interactions {
    public static final int QUOTES = 8;
    public static final int COMPLIMENTS = 6;
    private static final Item[] FAIRY_GIFTS = {Items.SUGAR, Items.SWEET_BERRIES, Items.GLOW_BERRIES, Items.HONEYCOMB,
            Items.FEATHER, Items.CORNFLOWER, Items.LILY_OF_THE_VALLEY, Items.AMETHYST_SHARD};

    private Interactions() {
    }

    static InteractionResult use(DecorBlock block, BlockState state, Level level, BlockPos pos, Player player,
                                 ItemStack held) {
        DecorFamily family = block.family();
        Interaction kind = family.interaction();
        if (kind == Interaction.FILL_BOTTLE && held.is(Items.GLASS_BOTTLE)) {
            return fillBottle(level, pos, player, held);
        }
        if (!held.isEmpty()) {
            return InteractionResult.PASS;
        }
        boolean client = level.isClientSide();
        switch (kind) {
            case TOGGLE -> {
                if (!client) {
                    boolean open = !state.getValue(DecorBlock.OPEN);
                    level.setBlock(pos, state.setValue(DecorBlock.OPEN, open), Block.UPDATE_ALL);
                    level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.8F, open ? 1.2F : 0.9F);
                    level.gameEvent(player, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
                }
            }
            case READ_BOOK -> {
                if (!client) {
                    say(player, "message.cottagecoreliving.quote." + level.getRandom().nextInt(QUOTES));
                    level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }
            case COMPLIMENT -> {
                if (!client) {
                    say(player, "message.cottagecoreliving.compliment." + level.getRandom().nextInt(COMPLIMENTS));
                    level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 1.3F);
                    particles(level, ParticleTypes.HEART, pos, 0.9D, 3, 0.25D);
                }
            }
            case CHIRP -> {
                if (!client) {
                    say(player, "message.cottagecoreliving.chirp");
                    level.playSound(null, pos, SoundEvents.PARROT_AMBIENT, SoundSource.BLOCKS, 0.8F,
                            1.6F + level.getRandom().nextFloat() * 0.3F);
                    note(level, pos, 0.75D, level.getRandom().nextFloat());
                }
            }
            case CHIME -> {
                if (!client) {
                    chime(level, pos, 0.8F);
                }
            }
            case WATER_PLANTS -> {
                return waterPlants(block, state, level, pos, player);
            }
            case FILL_BOTTLE -> {
                if (!client) {
                    level.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.3F, 1.4F);
                    particles(level, ParticleTypes.SPLASH, pos, 0.7D, 8, 0.25D);
                }
            }
            case SHAKE -> {
                if (!client) {
                    level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.8F, 1.6F);
                    particles(level, ParticleTypes.SNOWFLAKE, pos, 0.4D, 12, 0.15D);
                }
            }
            case STEAM -> {
                return steam(block, level, pos, player);
            }
            case KNOCK -> {
                return knock(block, state, level, pos, player);
            }
            case CYCLE_STYLE -> {
                if (!client) {
                    int next = (state.getValue(DecorBlock.STYLE) + 1) % family.styles();
                    level.setBlock(pos, state.setValue(DecorBlock.STYLE, next), Block.UPDATE_ALL);
                    level.playSound(null, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }
            default -> {
                return InteractionResult.PASS;
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static void say(Player player, String key, Object... args) {
        player.sendOverlayMessage(Component.translatable(key, args).withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    public static void particles(Level level, ParticleOptions type, BlockPos pos, double height, int count, double spread) {
        if (level instanceof ServerLevel server) {
            server.sendParticles(type, pos.getX() + 0.5D, pos.getY() + height, pos.getZ() + 0.5D, count, spread,
                    spread * 0.5D, spread, 0.01D);
        }
    }

    private static void note(Level level, BlockPos pos, double height, float color) {
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.NOTE, pos.getX() + 0.5D, pos.getY() + height, pos.getZ() + 0.5D, 0,
                    color, 0.0D, 0.0D, 1.0D);
        }
    }

    /** A little three-note chord, as a wind chime makes when you brush it. */
    public static void chime(Level level, BlockPos pos, float volume) {
        SoundEvent bell = SoundEvents.NOTE_BLOCK_CHIME.value();
        float[] pitches = {1.0F, 1.26F, 1.5F};
        for (float pitch : pitches) {
            level.playSound(null, pos, bell, SoundSource.BLOCKS, volume * 0.6F, pitch);
        }
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, volume, 1.2F);
        note(level, pos, 0.4D, 0.4F);
    }

    private static InteractionResult fillBottle(Level level, BlockPos pos, Player player, ItemStack held) {
        if (!CottageConfig.COMMON.fountainsFillBottles.get()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            ItemStack water = PotionContents.createItemStack(Items.POTION, Potions.WATER);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemUtils.createFilledResult(held, player, water));
            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
        }
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult waterPlants(DecorBlock block, BlockState state, Level level, BlockPos pos,
                                                 Player player) {
        if (player.getCooldowns().isOnCooldown(block.asItem().getDefaultInstance())) {
            return InteractionResult.SUCCESS;
        }
        if (level instanceof ServerLevel server) {
            Direction facing = block.facing(state);
            BlockPos center = pos.relative(facing, 2);
            boolean grow = CottageConfig.COMMON.wateringCanGrowsCrops.get();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    for (int dy = 0; dy >= -1; dy--) {
                        BlockPos p = center.offset(dx, dy, dz);
                        BlockState target = server.getBlockState(p);
                        boolean plant = target.is(BlockTags.CROPS) || target.is(BlockTags.SAPLINGS)
                                || target.getBlock() instanceof VegetationBlock;
                        if (!plant) {
                            continue;
                        }
                        server.sendParticles(ParticleTypes.SPLASH, p.getX() + 0.5D, p.getY() + 0.9D, p.getZ() + 0.5D,
                                6, 0.3D, 0.1D, 0.3D, 0.0D);
                        if (grow && target.isRandomlyTicking() && server.getRandom().nextBoolean()) {
                            target.randomTick(server, p, server.getRandom());
                        }
                    }
                }
            }
            server.sendParticles(ParticleTypes.FALLING_WATER, center.getX() + 0.5D, center.getY() + 1.2D,
                    center.getZ() + 0.5D, 16, 1.0D, 0.2D, 1.0D, 0.0D);
            level.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.4F, 1.5F);
            say(player, "message.cottagecoreliving.watered");
            player.getCooldowns().addCooldown(block.asItem().getDefaultInstance(), 20);
        }
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult steam(DecorBlock block, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide()) {
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.25F, 1.8F);
            particles(level, ParticleTypes.CLOUD, pos, 0.6D, 4, 0.12D);
            say(player, "message.cottagecoreliving.steam");
            if (CottageConfig.COMMON.cozyEffects.get() && !player.getCooldowns().isOnCooldown(block.asItem().getDefaultInstance())) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
                player.getCooldowns().addCooldown(block.asItem().getDefaultInstance(), 600);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult knock(DecorBlock block, BlockState state, Level level, BlockPos pos, Player player) {
        if (state.getValue(DecorBlock.OPEN)) {
            return InteractionResult.SUCCESS;
        }
        if (level instanceof ServerLevel server) {
            server.playSound(null, pos, SoundEvents.WOODEN_BUTTON_CLICK_ON, SoundSource.BLOCKS, 1.0F, 1.4F);
            server.setBlock(pos, state.setValue(DecorBlock.OPEN, true), Block.UPDATE_ALL);
            server.playSound(null, pos, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 0.5F, 1.6F);
            server.scheduleTick(pos, block, 60);
            particles(server, ParticleTypes.HAPPY_VILLAGER, pos, 0.4D, 8, 0.3D);
            say(player, "message.cottagecoreliving.knock");
            if (CottageConfig.COMMON.fairyGifts.get() && !player.getCooldowns().isOnCooldown(block.asItem().getDefaultInstance())
                    && server.getRandom().nextFloat() < 0.2F) {
                ItemStack gift = new ItemStack(FAIRY_GIFTS[server.getRandom().nextInt(FAIRY_GIFTS.length)]);
                Block.popResourceFromFace(server, pos, block.facing(state), gift);
                player.getCooldowns().addCooldown(block.asItem().getDefaultInstance(), 200);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
