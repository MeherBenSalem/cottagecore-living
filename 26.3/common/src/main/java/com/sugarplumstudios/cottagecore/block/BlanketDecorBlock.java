package com.sugarplumstudios.cottagecore.block;

import com.sugarplumstudios.cottagecore.catalog.DecorFamily;
import com.sugarplumstudios.cottagecore.catalog.DecorVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.Nullable;

/** A picnic blanket: neighbouring blankets join into one big blanket with a single fringed border. */
public class BlanketDecorBlock extends DecorBlock {
    public BlanketDecorBlock(DecorFamily family, DecorVariant variant) {
        super(family, variant);
        registerDefaultState(defaultBlockState().setValue(TableDecorBlock.NORTH, false).setValue(TableDecorBlock.EAST, false)
                .setValue(TableDecorBlock.SOUTH, false).setValue(TableDecorBlock.WEST, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TableDecorBlock.NORTH, TableDecorBlock.EAST, TableDecorBlock.SOUTH, TableDecorBlock.WEST);
    }

    private static boolean joins(BlockState neighbor) {
        return neighbor.getBlock() instanceof BlanketDecorBlock;
    }

    @Nullable
    @Override
    protected BlockState placementState(BlockPlaceContext context, BlockState state) {
        for (Direction d : Direction.Plane.HORIZONTAL) {
            state = state.setValue(TableDecorBlock.side(d),
                    joins(context.getLevel().getBlockState(context.getClickedPos().relative(d))));
        }
        return state;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        BlockState result = super.updateShape(state, level, ticks, pos, direction, neighborPos, neighbor, random);
        if (direction.getAxis().isHorizontal() && result.is(this)) {
            result = result.setValue(TableDecorBlock.side(direction), joins(neighbor));
        }
        return result;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        BlockState out = state;
        for (Direction d : Direction.Plane.HORIZONTAL) {
            out = out.setValue(TableDecorBlock.side(rotation.rotate(d)), state.getValue(TableDecorBlock.side(d)));
        }
        return out;
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        BlockState out = state;
        for (Direction d : Direction.Plane.HORIZONTAL) {
            out = out.setValue(TableDecorBlock.side(mirror.mirror(d)), state.getValue(TableDecorBlock.side(d)));
        }
        return out;
    }
}
