package com.sugarplumstudios.cottagecore.block;

import com.sugarplumstudios.cottagecore.catalog.DecorFamily;
import com.sugarplumstudios.cottagecore.catalog.DecorVariant;
import com.sugarplumstudios.cottagecore.config.CottageConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * A clock whose hour hand follows the time of day. It wakes itself once per
 * in-game hour with a scheduled tick (plus the odd random tick in case the time
 * was changed with a command), so a room full of clocks costs next to nothing.
 */
public class ClockDecorBlock extends DecorBlock {
    public static final IntegerProperty HOUR = IntegerProperty.create("hour", 0, 11);
    private static final int TICKS_PER_HOUR = 1000;

    public ClockDecorBlock(DecorFamily family, DecorVariant variant) {
        super(family, variant);
        registerDefaultState(defaultBlockState().setValue(HOUR, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HOUR);
    }

    /** The hour shown on a 12-hour dial (day time 0 is six in the morning). */
    public static int dialHour(Level level) {
        return (int) (((level.getOverworldClockTime() / TICKS_PER_HOUR) + 6L) % 12L);
    }

    @Nullable
    @Override
    protected BlockState placementState(BlockPlaceContext context, BlockState state) {
        return state.setValue(HOUR, dialHour(context.getLevel()));
    }

    private boolean isCuckoo() {
        return family.id().equals("cuckoo_clock");
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        super.onPlace(state, level, pos, oldState, moved);
        if (!level.isClientSide() && !oldState.is(this)) {
            scheduleNextHour(level, pos);
        }
    }

    private void scheduleNextHour(Level level, BlockPos pos) {
        int into = (int) (level.getOverworldClockTime() % TICKS_PER_HOUR);
        level.scheduleTick(pos, this, Math.max(20, TICKS_PER_HOUR - into + 1));
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int hour = dialHour(level);
        if (hour != state.getValue(HOUR)) {
            level.setBlock(pos, state.setValue(HOUR, hour), Block.UPDATE_CLIENTS);
            if (isCuckoo() && CottageConfig.COMMON.cuckooCalls.get()) {
                cuckoo(level, pos, 1.19F);
                level.scheduleTick(pos, this, 6);
                return;
            }
        } else if (isCuckoo() && CottageConfig.COMMON.cuckooCalls.get() && level.getOverworldClockTime() % TICKS_PER_HOUR < 30) {
            cuckoo(level, pos, 0.94F);
        }
        scheduleNextHour(level, pos);
    }

    private static void cuckoo(ServerLevel level, BlockPos pos, float pitch) {
        level.playSound(null, pos, SoundEvents.NOTE_BLOCK_FLUTE.value(), SoundSource.BLOCKS, 0.9F, pitch);
        level.sendParticles(ParticleTypes.NOTE, pos.getX() + 0.5D, pos.getY() + 0.9D, pos.getZ() + 0.5D, 0,
                pitch > 1 ? 0.3D : 0.6D, 0.0D, 0.0D, 1.0D);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int hour = dialHour(level);
        if (hour != state.getValue(HOUR)) {
            level.setBlock(pos, state.setValue(HOUR, hour), Block.UPDATE_CLIENTS);
        }
        if (!level.getBlockTicks().hasScheduledTick(pos, this)) {
            scheduleNextHour(level, pos);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (hand == InteractionHand.MAIN_HAND && !player.isSecondaryUseActive() && player.getMainHandItem().isEmpty()) {
            if (!level.isClientSide()) {
                player.sendOverlayMessage(timeOfDay(level.getOverworldClockTime()).withStyle(ChatFormatting.LIGHT_PURPLE));
                level.playSound(null, pos, SoundEvents.WOODEN_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.3F, 1.8F);
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    /** "It's 4:30 in the afternoon". */
    public static MutableComponent timeOfDay(long dayTime) {
        long ticks = Math.floorMod(dayTime, 24000L);
        int hour24 = (int) ((ticks / TICKS_PER_HOUR + 6) % 24);
        int minute = (int) (ticks % TICKS_PER_HOUR * 60 / TICKS_PER_HOUR);
        int hour12 = hour24 % 12 == 0 ? 12 : hour24 % 12;
        String period;
        if (hour24 >= 5 && hour24 < 12) {
            period = "message.cottagecoreliving.am";
        } else if (hour24 >= 12 && hour24 < 18) {
            period = "message.cottagecoreliving.pm";
        } else if (hour24 >= 18 && hour24 < 22) {
            period = "message.cottagecoreliving.evening";
        } else {
            period = "message.cottagecoreliving.night";
        }
        return Component.translatable("message.cottagecoreliving.time", hour12, String.format("%02d", minute),
                Component.translatable(period));
    }
}
