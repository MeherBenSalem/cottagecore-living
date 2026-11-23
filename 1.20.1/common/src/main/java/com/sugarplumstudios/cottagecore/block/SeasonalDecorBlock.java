package com.sugarplumstudios.cottagecore.block;

import com.sugarplumstudios.cottagecore.catalog.DecorFamily;
import com.sugarplumstudios.cottagecore.catalog.DecorVariant;
import com.sugarplumstudios.cottagecore.catalog.Season;
import com.sugarplumstudios.cottagecore.season.SeasonManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Wreaths, garlands, centerpieces, little trees and doormats that dress themselves
 * for the season. They follow the world's season (see {@link SeasonManager}) using
 * random ticks, unless a player has picked a season for them. Right-click cycles:
 * following the world, then spring, summer, autumn, winter, then back to following.
 */
public class SeasonalDecorBlock extends DecorBlock {
    public static final EnumProperty<Season> SEASON = EnumProperty.create("season", Season.class);
    public static final BooleanProperty PINNED = BooleanProperty.create("pinned");

    public SeasonalDecorBlock(DecorFamily family, DecorVariant variant) {
        super(family, variant);
        registerDefaultState(defaultBlockState().setValue(SEASON, Season.SPRING).setValue(PINNED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SEASON, PINNED);
    }

    @Nullable
    @Override
    protected BlockState placementState(BlockPlaceContext context, BlockState state) {
        return state.setValue(SEASON, SeasonManager.current(context.getLevel())).setValue(PINNED, false);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return !state.getValue(PINNED);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Season now = SeasonManager.current(level);
        if (!state.getValue(PINNED) && state.getValue(SEASON) != now) {
            level.setBlock(pos, state.setValue(SEASON, now), Block.UPDATE_CLIENTS);
        }
    }

    /** The state after one right-click. */
    public static BlockState cycle(BlockState state, Season worldSeason) {
        if (!state.getValue(PINNED)) {
            return state.setValue(PINNED, true).setValue(SEASON, Season.SPRING);
        }
        if (state.getValue(SEASON) == Season.WINTER) {
            return state.setValue(PINNED, false).setValue(SEASON, worldSeason);
        }
        return state.setValue(SEASON, state.getValue(SEASON).next());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (hand == InteractionHand.MAIN_HAND && !player.isSecondaryUseActive() && player.getMainHandItem().isEmpty()) {
            if (!level.isClientSide) {
                Season world = SeasonManager.current(level);
                BlockState next = cycle(state, world);
                level.setBlock(pos, next, Block.UPDATE_ALL);
                level.playSound(null, pos, SoundEvents.AZALEA_LEAVES_PLACE, SoundSource.BLOCKS, 0.8F, 1.2F);
                Component message = next.getValue(PINNED)
                        ? Component.translatable("message.cottagecoreliving.season_pinned", next.getValue(SEASON).displayName())
                        : Component.translatable("message.cottagecoreliving.season_follow", world.displayName());
                player.displayClientMessage(message.copy().withStyle(ChatFormatting.LIGHT_PURPLE), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.use(state, level, pos, player, hand, hit);
    }
}
