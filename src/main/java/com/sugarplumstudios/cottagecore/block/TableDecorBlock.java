package com.sugarplumstudios.cottagecore.block;

import com.sugarplumstudios.cottagecore.catalog.DecorFamily;
import com.sugarplumstudios.cottagecore.catalog.DecorSet;
import com.sugarplumstudios.cottagecore.catalog.DecorVariant;
import com.sugarplumstudios.cottagecore.item.TableclothItem;
import com.sugarplumstudios.cottagecore.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A full-height cottage table: decorations stand right on top of it. Tables side
 * by side join (inner legs disappear) and can be dressed with a tablecloth whose
 * drape only hangs over the outer edges.
 */
public class TableDecorBlock extends DecorBlock {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final EnumProperty<Cloth> CLOTH = EnumProperty.create("cloth", Cloth.class);

    private static final VoxelShape TOP = ShapeUtil.px(0, 14, 0, 16, 16, 16);
    private static final VoxelShape LEG_NW = ShapeUtil.px(1, 0, 1, 3, 14, 3);
    private static final VoxelShape LEG_NE = ShapeUtil.px(13, 0, 1, 15, 14, 3);
    private static final VoxelShape LEG_SE = ShapeUtil.px(13, 0, 13, 15, 14, 15);
    private static final VoxelShape LEG_SW = ShapeUtil.px(1, 0, 13, 3, 14, 15);

    public TableDecorBlock(DecorFamily family, DecorVariant variant) {
        super(family, variant);
        registerDefaultState(defaultBlockState().setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false)
                .setValue(WEST, false).setValue(CLOTH, Cloth.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(NORTH, EAST, SOUTH, WEST, CLOTH);
    }

    public static BooleanProperty side(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            default -> WEST;
        };
    }

    private static boolean joins(BlockState neighbor) {
        return neighbor.getBlock() instanceof TableDecorBlock;
    }

    @Nullable
    @Override
    protected BlockState placementState(BlockPlaceContext context, BlockState state) {
        for (Direction d : Direction.Plane.HORIZONTAL) {
            state = state.setValue(side(d), joins(context.getLevel().getBlockState(context.getClickedPos().relative(d))));
        }
        return state;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        BlockState result = super.updateShape(state, direction, neighbor, level, pos, neighborPos);
        if (direction.getAxis().isHorizontal() && result.is(this)) {
            result = result.setValue(side(direction), joins(neighbor));
        }
        return result;
    }

    @Override
    protected VoxelShape computeShape(BlockState state) {
        VoxelShape shape = TOP;
        boolean n = state.getValue(NORTH);
        boolean e = state.getValue(EAST);
        boolean s = state.getValue(SOUTH);
        boolean w = state.getValue(WEST);
        if (!n && !w) {
            shape = net.minecraft.world.phys.shapes.Shapes.or(shape, LEG_NW);
        }
        if (!n && !e) {
            shape = net.minecraft.world.phys.shapes.Shapes.or(shape, LEG_NE);
        }
        if (!s && !e) {
            shape = net.minecraft.world.phys.shapes.Shapes.or(shape, LEG_SE);
        }
        if (!s && !w) {
            shape = net.minecraft.world.phys.shapes.Shapes.or(shape, LEG_SW);
        }
        return shape.optimize();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        Cloth current = state.getValue(CLOTH);
        if (held.getItem() instanceof TableclothItem cloth) {
            Cloth wanted = Cloth.of(cloth.set());
            if (wanted == current) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide) {
                dropCloth(level, pos, current);
                level.setBlock(pos, state.setValue(CLOTH, wanted), Block.UPDATE_ALL);
                level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F, 1.1F);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.is(Items.SHEARS) && current != Cloth.NONE) {
            if (!level.isClientSide) {
                dropCloth(level, pos, current);
                level.setBlock(pos, state.setValue(CLOTH, Cloth.NONE), Block.UPDATE_ALL);
                level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 0.8F, 1.2F);
                level.gameEvent(player, GameEvent.SHEAR, pos);
                held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    private static void dropCloth(Level level, BlockPos pos, Cloth cloth) {
        DecorSet set = cloth.set();
        if (set != null) {
            Block.popResourceFromFace(level, pos, Direction.UP, new ItemStack(ModItems.tablecloth(set)));
        }
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        BlockState out = state;
        for (Direction d : Direction.Plane.HORIZONTAL) {
            out = out.setValue(side(rotation.rotate(d)), state.getValue(side(d)));
        }
        return out;
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        BlockState out = state;
        for (Direction d : Direction.Plane.HORIZONTAL) {
            out = out.setValue(side(mirror.mirror(d)), state.getValue(side(d)));
        }
        return out;
    }
}
