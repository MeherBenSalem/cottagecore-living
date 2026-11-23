package com.sugarplumstudios.cottagecore.block;

import com.sugarplumstudios.cottagecore.catalog.DecorFamily;
import com.sugarplumstudios.cottagecore.catalog.DecorVariant;
import com.sugarplumstudios.cottagecore.catalog.Interaction;
import com.sugarplumstudios.cottagecore.catalog.LightMode;
import com.sugarplumstudios.cottagecore.catalog.Mount;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Every decoration in the mod is (a subclass of) this block. Which block-state
 * properties it has depends on its {@link DecorFamily}: facing, mount (floor / wall /
 * ceiling), count (stacking), lit, open and style. Hit boxes come from the
 * generated catalog and are turned to match the model.
 */
public class DecorBlock extends Block implements SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final IntegerProperty COUNT = IntegerProperty.create("count", 1, 3);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final IntegerProperty STYLE = IntegerProperty.create("style", 0, 7);
    /** Standing on something thin (a blanket, a carpet): the model drops 15 pixels to sit on it. */
    public static final BooleanProperty LOW = BooleanProperty.create("low");
    private static final double LOW_DROP = 15.0D / 16.0D;

    /** The family being constructed: createBlockStateDefinition runs inside Block's constructor. */
    private static final ThreadLocal<DecorFamily> BUILDING = new ThreadLocal<>();
    private static final ThreadLocal<EnumProperty<Mount>> BUILDING_MOUNT = new ThreadLocal<>();

    protected final DecorFamily family;
    protected final DecorVariant variant;
    @Nullable
    private final EnumProperty<Mount> mountProperty;
    private final Map<BlockState, VoxelShape> shapes = new IdentityHashMap<>();

    public DecorBlock(DecorFamily family, DecorVariant variant) {
        super(prepare(family));
        this.family = family;
        this.variant = variant;
        this.mountProperty = BUILDING_MOUNT.get();
        BUILDING.remove();
        BUILDING_MOUNT.remove();
        BlockState state = stateDefinition.any();
        if (family.hasFacing()) {
            state = state.setValue(FACING, Direction.NORTH);
        }
        if (mountProperty != null) {
            state = state.setValue(mountProperty, family.defaultMount());
        }
        if (family.stacks()) {
            state = state.setValue(COUNT, 1);
        }
        if (family.hasLitProperty()) {
            state = state.setValue(LIT, false);
        }
        if (family.openable()) {
            state = state.setValue(OPEN, false);
        }
        if (family.styles() > 0) {
            state = state.setValue(STYLE, 0);
        }
        if (family.sitsLow()) {
            state = state.setValue(LOW, false);
        }
        registerDefaultState(state.setValue(WATERLOGGED, false));
        for (BlockState s : stateDefinition.getPossibleStates()) {
            shapes.put(s, computeShape(s));
        }
    }

    private static Properties prepare(DecorFamily family) {
        BUILDING.set(family);
        BUILDING_MOUNT.set(family.hasMountProperty() ? EnumProperty.create("mount", Mount.class, family.mounts()) : null);
        Properties props = Properties.of()
                .mapColor(family.material().mapColor())
                .sound(family.material().sound())
                .strength(family.material().strength())
                .noOcclusion()
                .isViewBlocking((s, l, p) -> false)
                .isSuffocating((s, l, p) -> false)
                .isRedstoneConductor((s, l, p) -> false)
                .pushReaction(PushReaction.DESTROY);
        if (family.walkThrough()) {
            props.noCollission();
        }
        if (family.material().flammable()) {
            props.ignitedByLava();
        }
        if (family.lightMode() != LightMode.NONE) {
            props.lightLevel(state -> lightFor(family, state));
        }
        if (family.seasonal() || family.hasHours()) {
            props.randomTicks();
        }
        return props;
    }

    private static int lightFor(DecorFamily family, BlockState state) {
        return switch (family.lightMode()) {
            case ALWAYS -> family.lightLevel();
            case SWITCH -> state.getValue(LIT) ? family.lightLevel() : 0;
            case CANDLE -> state.getValue(LIT)
                    ? Math.min(15, family.lightLevel() * (state.hasProperty(COUNT) ? state.getValue(COUNT) : 1)) : 0;
            default -> 0;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        DecorFamily f = BUILDING.get();
        builder.add(WATERLOGGED);
        if (f == null) {
            return;
        }
        if (f.hasFacing()) {
            builder.add(FACING);
        }
        EnumProperty<Mount> mount = BUILDING_MOUNT.get();
        if (mount != null) {
            builder.add(mount);
        }
        if (f.stacks()) {
            builder.add(COUNT);
        }
        if (f.hasLitProperty()) {
            builder.add(LIT);
        }
        if (f.openable()) {
            builder.add(OPEN);
        }
        if (f.styles() > 0) {
            builder.add(STYLE);
        }
        if (f.sitsLow()) {
            builder.add(LOW);
        }
    }

    public DecorFamily family() {
        return family;
    }

    public DecorVariant variant() {
        return variant;
    }

    // ------------------------------------------------------------------ mounting

    public Mount mount(BlockState state) {
        return mountProperty != null ? state.getValue(mountProperty) : family.defaultMount();
    }

    @Nullable
    public EnumProperty<Mount> mountProperty() {
        return mountProperty;
    }

    public Direction facing(BlockState state) {
        return state.hasProperty(FACING) ? state.getValue(FACING) : Direction.NORTH;
    }

    /** The side the decoration hangs or stands on. */
    protected Direction supportSide(BlockState state) {
        return switch (mount(state)) {
            case FLOOR -> Direction.DOWN;
            case CEILING -> Direction.UP;
            case WALL -> facing(state).getOpposite();
        };
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction side = supportSide(state);
        BlockPos supportPos = pos.relative(side);
        BlockState support = level.getBlockState(supportPos);
        if (support.getBlock() instanceof DecorBlock decor && decor.family.walkThrough()) {
            return false;
        }
        return !support.getCollisionShape(level, supportPos).isEmpty();
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState existing = level.getBlockState(pos);
        if (existing.is(this) && family.stacks()) {
            return existing.setValue(COUNT, Math.min(family.maxCount(), existing.getValue(COUNT) + 1));
        }
        boolean water = level.getFluidState(pos).getType() == Fluids.WATER;
        Direction look = context.getHorizontalDirection();
        for (Mount mount : mountOrder(context.getClickedFace())) {
            if (!family.mounts().contains(mount)) {
                continue;
            }
            Direction facing = look.getOpposite();
            if (mount == Mount.WALL) {
                facing = context.getClickedFace().getAxis().isHorizontal() ? context.getClickedFace() : look.getOpposite();
            }
            BlockState state = defaultBlockState().setValue(WATERLOGGED, water);
            if (state.hasProperty(FACING)) {
                state = state.setValue(FACING, facing);
            }
            if (mountProperty != null) {
                state = state.setValue(mountProperty, mount);
            }
            if (state.hasProperty(LOW)) {
                state = state.setValue(LOW, mount == Mount.FLOOR && isThinSurface(level, pos.below()));
            }
            state = placementState(context, state);
            if (state != null && state.canSurvive(level, pos)) {
                return state;
            }
        }
        return null;
    }

    /** Lets subclasses adjust a candidate placement (clock hands, seasons, curtain rods). */
    @Nullable
    protected BlockState placementState(BlockPlaceContext context, BlockState state) {
        return state;
    }

    private static List<Mount> mountOrder(Direction clicked) {
        List<Mount> order = new ArrayList<>(3);
        if (clicked == Direction.DOWN) {
            order.add(Mount.CEILING);
            order.add(Mount.WALL);
            order.add(Mount.FLOOR);
        } else if (clicked == Direction.UP) {
            order.add(Mount.FLOOR);
            order.add(Mount.WALL);
            order.add(Mount.CEILING);
        } else {
            order.add(Mount.WALL);
            order.add(Mount.FLOOR);
            order.add(Mount.CEILING);
        }
        return order;
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        if (family.stacks() && !context.isSecondaryUseActive() && context.getItemInHand().is(asItem())
                && state.getValue(COUNT) < family.maxCount()) {
            return true;
        }
        return super.canBeReplaced(state, context);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        if (direction == supportSide(state) && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (direction == Direction.DOWN && state.hasProperty(LOW)) {
            state = state.setValue(LOW, mount(state) == Mount.FLOOR && isThinSurface(level, neighborPos));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    /** A carpet, a picnic blanket or anything else whose top is at most two pixels high. */
    public static boolean isThinSurface(BlockGetter level, BlockPos pos) {
        VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos);
        return !shape.isEmpty() && shape.max(Direction.Axis.Y) <= 2.0D / 16.0D + 1.0E-6D;
    }

    /** How far the model is lowered, in blocks. */
    public static double drop(BlockState state) {
        return state.hasProperty(LOW) && state.getValue(LOW) ? LOW_DROP : 0.0D;
    }

    // ------------------------------------------------------------------ water

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluid) {
        if (family.lightMode() == LightMode.CANDLE && state.getValue(LIT) && fluid.getType() == Fluids.WATER) {
            level.playSound(null, pos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
            state = state.setValue(LIT, false);
        }
        return SimpleWaterloggedBlock.super.placeLiquid(level, pos, state, fluid);
    }

    // ------------------------------------------------------------------ shapes

    protected VoxelShape computeShape(BlockState state) {
        VoxelShape north = family.shape(shapeKey(state));
        VoxelShape shape = state.hasProperty(FACING) ? ShapeUtil.rotate(north, state.getValue(FACING)) : north;
        double drop = drop(state);
        return drop > 0 ? shape.move(0, -drop, 0) : shape;
    }

    /** Matches Family.shape_key in tools/clgen/fam.py. */
    protected String shapeKey(BlockState state) {
        StringBuilder key = new StringBuilder();
        if (mountProperty != null) {
            key.append(state.getValue(mountProperty).getSerializedName());
        }
        if (family.stacks()) {
            if (!key.isEmpty()) {
                key.append('_');
            }
            key.append('c').append(state.getValue(COUNT));
        }
        return key.isEmpty() ? "base" : key.toString();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = shapes.get(state);
        return shape != null ? shape : computeShape(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return family.walkThrough() ? Shapes.empty() : getShape(state, level, pos, context);
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return false;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return !state.getValue(WATERLOGGED);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return family.walkThrough() && type == PathComputationType.LAND;
    }

    // ------------------------------------------------------------------ rotation

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.hasProperty(FACING) ? state.setValue(FACING, rotation.rotate(state.getValue(FACING))) : state;
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.hasProperty(FACING) ? state.rotate(mirror.getRotation(state.getValue(FACING))) : state;
    }

    /** Sneak + right-click with an empty hand turns floor and ceiling decorations a quarter turn. */
    public boolean canTurn(BlockState state) {
        return state.hasProperty(FACING) && mount(state) != Mount.WALL;
    }

    // ------------------------------------------------------------------ interaction

    /**
     * Since 1.20.5 a right-click reaches a block as useItemOn (with whatever is held, even nothing) and then, if that
     * passes, useWithoutItem. Every decoration answers in {@link #use}, exactly as on 1.20.1.
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        InteractionResult result = use(state, level, pos, player, hand, hit);
        return switch (result) {
            case SUCCESS, SUCCESS_NO_ITEM_USED -> ItemInteractionResult.SUCCESS;
            case CONSUME -> ItemInteractionResult.CONSUME;
            case CONSUME_PARTIAL -> ItemInteractionResult.CONSUME_PARTIAL;
            case FAIL -> ItemInteractionResult.FAIL;
            default -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        };
    }

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (player.isSecondaryUseActive()) {
            if (held.isEmpty() && canTurn(state)) {
                if (!level.isClientSide) {
                    level.setBlock(pos, state.setValue(FACING, state.getValue(FACING).getClockWise()), Block.UPDATE_ALL);
                    level.playSound(null, pos, state.getSoundType().getPlaceSound(), SoundSource.BLOCKS,
                            0.6F, 1.2F);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            return InteractionResult.PASS;
        }
        InteractionResult light = useLight(state, level, pos, player, held);
        if (light.consumesAction()) {
            return light;
        }
        return Interactions.use(this, state, level, pos, player, held);
    }

    private InteractionResult useLight(BlockState state, Level level, BlockPos pos, Player player, ItemStack held) {
        if (!family.hasLitProperty()) {
            return InteractionResult.PASS;
        }
        boolean lit = state.getValue(LIT);
        boolean igniter = held.is(Items.FLINT_AND_STEEL) || held.is(Items.FIRE_CHARGE);
        if (!lit && igniter && !state.getValue(WATERLOGGED)) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(LIT, true), Block.UPDATE_ALL_IMMEDIATE);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                if (held.is(Items.FIRE_CHARGE)) {
                    level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, 1.2F);
                    if (!player.getAbilities().instabuild) {
                        held.shrink(1);
                    }
                } else {
                    level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    held.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.isEmpty() && (lit || family.lightMode() == LightMode.SWITCH)) {
            if (!level.isClientSide) {
                boolean now = !lit;
                if (now && state.getValue(WATERLOGGED)) {
                    return InteractionResult.PASS;
                }
                level.setBlock(pos, state.setValue(LIT, now), Block.UPDATE_ALL_IMMEDIATE);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                level.playSound(null, pos, now ? SoundEvents.FLINTANDSTEEL_USE : SoundEvents.CANDLE_EXTINGUISH,
                        SoundSource.BLOCKS, 0.8F, 1.0F);
                if (!now && level instanceof ServerLevel server) {
                    for (Vec3 flame : flamePositions(state, pos)) {
                        server.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE, flame.x, flame.y + 0.05D,
                                flame.z, 2, 0.01D, 0.02D, 0.01D, 0.0D);
                    }
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    /** World positions of candle flames for the current state. */
    public List<Vec3> flamePositions(BlockState state, BlockPos pos) {
        int count = state.hasProperty(COUNT) ? state.getValue(COUNT) : 1;
        List<Vec3> out = new ArrayList<>();
        Direction facing = facing(state);
        for (Vec3 px : family.flames(count)) {
            Vec3 local = ShapeUtil.pixelToWorld(facing, px);
            out.add(new Vec3(pos.getX() + local.x, pos.getY() + local.y - drop(state), pos.getZ() + local.z));
        }
        return out;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (family.interaction() == Interaction.KNOCK && state.hasProperty(OPEN) && state.getValue(OPEN)) {
            level.setBlock(pos, state.setValue(OPEN, false), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS, 0.5F, 1.6F);
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide && family.ambient() == com.sugarplumstudios.cottagecore.catalog.Ambient.LEAVES) {
            AmbientEffects.rustle(state, level, pos, entity);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        AmbientEffects.animate(this, state, level, pos, random);
    }
}
