package com.sugarplumstudios.cottagecore.block;

import com.sugarplumstudios.cottagecore.catalog.DecorFamily;
import com.sugarplumstudios.cottagecore.catalog.DecorVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Curtains hang against a wall. Only the topmost curtain of a column shows the
 * rod, so curtains stacked for a tall window read as one long drape, and
 * right-clicking any of them opens or closes the whole column.
 */
public class CurtainDecorBlock extends DecorBlock {
    public static final BooleanProperty ROD = BooleanProperty.create("rod");

    public CurtainDecorBlock(DecorFamily family, DecorVariant variant) {
        super(family, variant);
        registerDefaultState(defaultBlockState().setValue(ROD, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ROD);
    }

    private static boolean isCurtain(BlockState state) {
        return state.getBlock() instanceof CurtainDecorBlock;
    }

    @Nullable
    @Override
    protected BlockState placementState(BlockPlaceContext context, BlockState state) {
        BlockState above = context.getLevel().getBlockState(context.getClickedPos().above());
        boolean rod = !(isCurtain(above) && above.getValue(FACING) == state.getValue(FACING));
        if (!rod) {
            state = state.setValue(OPEN, above.getValue(OPEN));
        }
        return state.setValue(ROD, rod);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        BlockState result = super.updateShape(state, level, ticks, pos, direction, neighborPos, neighbor, random);
        if (direction == Direction.UP && result.is(this)) {
            boolean rod = !(isCurtain(neighbor) && neighbor.getValue(FACING) == result.getValue(FACING));
            result = result.setValue(ROD, rod);
        }
        return result;
    }

    /** Every curtain in the vertical run that includes {@code pos}, facing the same way. */
    public static List<BlockPos> column(BlockGetter level, BlockPos pos, Direction facing) {
        List<BlockPos> out = new ArrayList<>();
        out.add(pos);
        for (int dir = -1; dir <= 1; dir += 2) {
            BlockPos p = pos.above(dir);
            for (int i = 0; i < 32; i++) {
                BlockState s = level.getBlockState(p);
                if (!isCurtain(s) || s.getValue(FACING) != facing) {
                    break;
                }
                out.add(p);
                p = p.above(dir);
            }
        }
        return out;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (hand == InteractionHand.MAIN_HAND && !player.isSecondaryUseActive() && player.getMainHandItem().isEmpty()) {
            if (!level.isClientSide()) {
                boolean open = !state.getValue(OPEN);
                for (BlockPos p : column(level, pos, state.getValue(FACING))) {
                    BlockState s = level.getBlockState(p);
                    level.setBlock(p, s.setValue(OPEN, open), Block.UPDATE_ALL);
                }
                level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.8F, open ? 1.2F : 0.9F);
                level.gameEvent(player, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(state, level, pos, player, hand, hit);
    }
}
