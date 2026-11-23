package com.sugarplumstudios.cottagecore.gametest;

import com.mojang.authlib.GameProfile;
import com.sugarplumstudios.cottagecore.CottagecoreLiving;
import com.sugarplumstudios.cottagecore.block.BlanketDecorBlock;
import com.sugarplumstudios.cottagecore.block.ClockDecorBlock;
import com.sugarplumstudios.cottagecore.block.Cloth;
import com.sugarplumstudios.cottagecore.block.CurtainDecorBlock;
import com.sugarplumstudios.cottagecore.block.DecorBlock;
import com.sugarplumstudios.cottagecore.block.SeasonalDecorBlock;
import com.sugarplumstudios.cottagecore.block.TableDecorBlock;
import com.sugarplumstudios.cottagecore.block.entity.DecorStorageBlockEntity;
import com.sugarplumstudios.cottagecore.catalog.DecorSet;
import com.sugarplumstudios.cottagecore.catalog.Mount;
import com.sugarplumstudios.cottagecore.catalog.Season;
import com.sugarplumstudios.cottagecore.collection.CollectionData;
import com.sugarplumstudios.cottagecore.collection.Collectibles;
import com.sugarplumstudios.cottagecore.registry.ModBlocks;
import com.sugarplumstudios.cottagecore.registry.ModItems;
import com.sugarplumstudios.cottagecore.season.SeasonManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Automated in-game checks, run headlessly with {@code ./gradlew runGameTestServer}.
 * They place decorations the way a player would and use them, on the server.
 */
@GameTestHolder(CottagecoreLiving.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CottageGameTests {
    private static final String TEMPLATE = "empty";

    private CottageGameTests() {
    }

    // ------------------------------------------------------------------ helpers

    /** Places {@code item} the way a player would, by clicking {@code face} of the block at {@code target}. */
    private static InteractionResult place(GameTestHelper helper, Player player, Item item, BlockPos target, Direction face) {
        BlockPos pos = helper.absolutePos(target);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
        Vec3 hit = Vec3.atCenterOf(pos).relative(face, 0.5D);
        UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(hit, face, pos, false));
        InteractionResult result = player.getItemInHand(InteractionHand.MAIN_HAND).useOn(context);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        return result;
    }

    private static InteractionResult use(GameTestHelper helper, BlockPos relative, Player player) {
        BlockPos pos = helper.absolutePos(relative);
        BlockState state = helper.getLevel().getBlockState(pos);
        return state.use(helper.getLevel(), player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false));
    }

    private static Player player(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        return player;
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            helper.fail(message);
        }
    }

    private static BlockState at(GameTestHelper helper, BlockPos relative) {
        return helper.getBlockState(relative);
    }

    // ------------------------------------------------------------------ every decoration

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void everyDecorationStandsTurnsAndHasAShape(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(3, 2, 3));
        List<DecorBlock> blocks = ModBlocks.all();
        check(helper, blocks.size() >= 250, "expected hundreds of decorations, found " + blocks.size());
        for (DecorBlock block : blocks) {
            Mount mount = block.family().defaultMount();
            BlockPos support = switch (mount) {
                case FLOOR -> pos.below();
                case CEILING -> pos.above();
                case WALL -> pos.south();
            };
            level.setBlockAndUpdate(support, Blocks.STONE.defaultBlockState());
            BlockState state = block.defaultBlockState();
            check(helper, state.canSurvive(level, pos), block + " can't stand on its " + mount + " support");
            level.setBlockAndUpdate(pos, state);
            check(helper, level.getBlockState(pos).is(block), block + " vanished after placing");
            if (state.hasProperty(DecorBlock.FACING)) {
                check(helper, state.rotate(Rotation.CLOCKWISE_90).getValue(DecorBlock.FACING) == Direction.EAST,
                        block + " doesn't rotate");
            }
            for (BlockState s : block.getStateDefinition().getPossibleStates()) {
                check(helper, !s.getShape(level, pos).isEmpty(), block + " has an empty hit box in " + s);
            }
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(support, Blocks.AIR.defaultBlockState());
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void everyCollectibleHasARecipeAndLoot(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Set<Item> craftable = new HashSet<>();
        level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)
                .forEach(r -> craftable.add(r.getResultItem(level.registryAccess()).getItem()));
        for (Collectibles.Entry entry : Collectibles.all()) {
            check(helper, craftable.contains(entry.item()), "no recipe makes " + entry.id());
        }
        check(helper, craftable.contains(ModItems.COLLECTION_BOOK.get()), "no recipe makes the collection book");
        for (DecorBlock block : ModBlocks.all()) {
            LootTable table = level.getServer().getLootData().getLootTable(block.getLootTable());
            check(helper, table != LootTable.EMPTY, "no loot table for " + block);
        }
        check(helper, Collectibles.total() == ModBlocks.all().size() + DecorSet.values().length, "collectible count is off");
        helper.succeed();
    }

    // ------------------------------------------------------------------ placing like a player

    @GameTest(template = TEMPLATE)
    public static void candlesStackFaceThePlayerAndLight(GameTestHelper helper) {
        Player player = player(helper);
        BlockPos stone = new BlockPos(2, 1, 2);
        BlockPos candle = stone.above();
        helper.setBlock(stone, Blocks.STONE);
        Item candles = ModItems.decoration("strawberry_pillar_candles");
        place(helper, player, candles, stone, Direction.UP);
        BlockState placed = at(helper, candle);
        check(helper, placed.is(ModBlocks.get("strawberry_pillar_candles")), "candles weren't placed");
        check(helper, placed.getValue(DecorBlock.FACING) == player.getDirection().getOpposite(), "candles don't face the player");
        place(helper, player, candles, candle, Direction.UP);
        place(helper, player, candles, candle, Direction.UP);
        check(helper, at(helper, candle).getValue(DecorBlock.COUNT) == 3, "candles didn't stack to three");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FLINT_AND_STEEL));
        use(helper, candle, player);
        check(helper, at(helper, candle).getValue(DecorBlock.LIT), "flint and steel didn't light the candles");
        check(helper, at(helper, candle).getLightEmission() == 12, "three lit candles should give light 12");
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        use(helper, candle, player);
        check(helper, !at(helper, candle).getValue(DecorBlock.LIT), "an empty hand didn't snuff the candles");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void wallAndCeilingDecorationsHangAndFall(GameTestHelper helper) {
        Player player = player(helper);
        BlockPos wall = new BlockPos(2, 2, 2);
        helper.setBlock(wall, Blocks.STONE);
        place(helper, player, ModItems.decoration("pink_embroidery_hoop"), wall, Direction.WEST);
        BlockPos hoop = wall.west();
        check(helper, at(helper, hoop).is(ModBlocks.get("pink_embroidery_hoop")), "hoop wasn't hung on the wall");
        check(helper, at(helper, hoop).getValue(DecorBlock.FACING) == Direction.WEST, "hoop doesn't face away from the wall");
        place(helper, player, ModItems.decoration("lavender_garden_lantern"), wall, Direction.DOWN);
        BlockPos lantern = wall.below();
        DecorBlock lanternBlock = (DecorBlock) ModBlocks.get("lavender_garden_lantern");
        check(helper, at(helper, lantern).is(lanternBlock), "lantern wasn't hung");
        check(helper, lanternBlock.mount(at(helper, lantern)) == Mount.CEILING, "lantern didn't hang from the ceiling");
        use(helper, lantern, player);
        check(helper, at(helper, lantern).getValue(DecorBlock.LIT) && at(helper, lantern).getLightEmission() == 14,
                "lantern didn't switch on");
        helper.destroyBlock(wall);
        helper.runAfterDelay(2, () -> {
            check(helper, at(helper, hoop).isAir(), "hoop stayed after its wall was broken");
            check(helper, at(helper, lantern).isAir(), "lantern stayed after its ceiling was broken");
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE)
    public static void sneakingWithAnEmptyHandTurnsDecorations(GameTestHelper helper) {
        Player player = player(helper);
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos.below(), Blocks.STONE);
        helper.setBlock(pos, ModBlocks.get("fairy_teapot").defaultBlockState().setValue(DecorBlock.FACING, Direction.NORTH));
        player.setShiftKeyDown(true);
        use(helper, pos, player);
        check(helper, at(helper, pos).getValue(DecorBlock.FACING) == Direction.EAST, "sneak-click didn't turn the teapot");
        helper.succeed();
    }

    // ------------------------------------------------------------------ special decorations

    @GameTest(template = TEMPLATE)
    public static void clockHandsFollowTheTime(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        long saved = level.getDayTime();
        level.setDayTime(6000L);
        check(helper, ClockDecorBlock.dialHour(level) == 0, "noon should be twelve o'clock");
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos.south(), Blocks.STONE);
        Player player = player(helper);
        place(helper, player, ModItems.decoration("vintage_wall_clock"), pos.south(), Direction.NORTH);
        check(helper, at(helper, pos).getValue(ClockDecorBlock.HOUR) == 0, "clock should show twelve o'clock");
        level.setDayTime(9500L);
        BlockState state = at(helper, pos);
        state.getBlock().randomTick(state, level, helper.absolutePos(pos), level.random);
        check(helper, at(helper, pos).getValue(ClockDecorBlock.HOUR) == 3, "clock should show three o'clock");
        Object[] args = ((TranslatableContents) ClockDecorBlock.timeOfDay(9500L).getContents()).getArgs();
        check(helper, Integer.valueOf(3).equals(args[0]) && "30".equals(args[1]), "time of day reads wrong");
        level.setDayTime(saved);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void seasonalDecorationsCycleAndFollow(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos.below(), Blocks.STONE);
        Player player = player(helper);
        place(helper, player, ModItems.decoration("seasonal_tree"), pos.below(), Direction.UP);
        Season world = SeasonManager.current(level);
        check(helper, at(helper, pos).getValue(SeasonalDecorBlock.SEASON) == world, "tree didn't take the world's season");
        Season[] expected = {Season.SPRING, Season.SUMMER, Season.AUTUMN, Season.WINTER};
        for (Season season : expected) {
            use(helper, pos, player);
            check(helper, at(helper, pos).getValue(SeasonalDecorBlock.PINNED), "tree should be pinned");
            check(helper, at(helper, pos).getValue(SeasonalDecorBlock.SEASON) == season, "tree should show " + season);
        }
        use(helper, pos, player);
        check(helper, !at(helper, pos).getValue(SeasonalDecorBlock.PINNED), "tree should follow the world again");
        check(helper, at(helper, pos).getValue(SeasonalDecorBlock.SEASON) == world, "tree should show the world's season");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void trinketBoxesKeepAndDropTheirTreasures(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos.below(), Blocks.STONE);
        helper.setBlock(pos, ModBlocks.get("pink_trinket_box"));
        if (!(helper.getBlockEntity(pos) instanceof DecorStorageBlockEntity box)) {
            helper.fail("trinket box has no storage");
            return;
        }
        check(helper, box.getContainerSize() == 9, "trinket box should hold nine stacks");
        box.setItem(4, new ItemStack(Items.DIAMOND, 3));
        helper.destroyBlock(pos);
        helper.assertItemEntityPresent(Items.DIAMOND, pos, 2.0D);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void tablesJoinAndWearTablecloths(GameTestHelper helper) {
        Player player = player(helper);
        BlockPos a = new BlockPos(2, 2, 2);
        BlockPos b = a.east();
        helper.setBlock(a.below(), Blocks.STONE);
        helper.setBlock(b.below(), Blocks.STONE);
        place(helper, player, ModItems.decoration("oak_cottage_table"), a.below(), Direction.UP);
        place(helper, player, ModItems.decoration("cherry_cottage_table"), b.below(), Direction.UP);
        check(helper, at(helper, a).getValue(TableDecorBlock.EAST) && at(helper, b).getValue(TableDecorBlock.WEST),
                "tables side by side didn't join");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.tablecloth(DecorSet.LAVENDER_HOME)));
        use(helper, a, player);
        check(helper, at(helper, a).getValue(TableDecorBlock.CLOTH) == Cloth.LAVENDER, "tablecloth didn't go on");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));
        use(helper, a, player);
        check(helper, at(helper, a).getValue(TableDecorBlock.CLOTH) == Cloth.NONE, "shears didn't take the cloth off");
        helper.assertItemEntityPresent(ModItems.tablecloth(DecorSet.LAVENDER_HOME), a, 2.0D);
        // a decoration stands on the table
        place(helper, player, ModItems.decoration("strawberry_flower_vase"), a, Direction.UP);
        check(helper, at(helper, a.above()).is(ModBlocks.get("strawberry_flower_vase")), "vase didn't stand on the table");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void curtainsShareARodAndOpenTogether(GameTestHelper helper) {
        Player player = player(helper);
        BlockPos top = new BlockPos(2, 3, 2);
        BlockPos bottom = top.below();
        helper.setBlock(top.south(), Blocks.GLASS);
        helper.setBlock(bottom.south(), Blocks.GLASS);
        place(helper, player, ModItems.decoration("pink_curtains"), top.south(), Direction.NORTH);
        place(helper, player, ModItems.decoration("pink_curtains"), bottom.south(), Direction.NORTH);
        check(helper, at(helper, top).getBlock() instanceof CurtainDecorBlock, "top curtain missing");
        check(helper, at(helper, top).getValue(CurtainDecorBlock.ROD), "top curtain should show the rod");
        check(helper, !at(helper, bottom).getValue(CurtainDecorBlock.ROD), "lower curtain shouldn't show a rod");
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        use(helper, bottom, player);
        check(helper, at(helper, top).getValue(DecorBlock.OPEN) && at(helper, bottom).getValue(DecorBlock.OPEN),
                "the whole column should open");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void picnicBlanketsJoin(GameTestHelper helper) {
        Player player = player(helper);
        BlockPos a = new BlockPos(2, 2, 2);
        helper.setBlock(a.below(), Blocks.GRASS_BLOCK);
        helper.setBlock(a.below().north(), Blocks.GRASS_BLOCK);
        place(helper, player, ModItems.decoration("strawberry_picnic_blanket"), a.below(), Direction.UP);
        place(helper, player, ModItems.decoration("fairy_picnic_blanket"), a.below().north(), Direction.UP);
        check(helper, at(helper, a).getBlock() instanceof BlanketDecorBlock, "blanket missing");
        check(helper, at(helper, a).getValue(TableDecorBlock.NORTH) && at(helper, a.north()).getValue(TableDecorBlock.SOUTH),
                "blankets didn't join");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 120)
    public static void interactionsDoTheirThing(GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        BlockPos frame = new BlockPos(1, 2, 2);
        BlockPos door = new BlockPos(3, 2, 2);
        BlockPos fountain = new BlockPos(5, 2, 2);
        helper.setBlock(frame.south(), Blocks.STONE);
        helper.setBlock(door.south(), Blocks.STONE);
        helper.setBlock(fountain.below(), Blocks.STONE);
        place(helper, player, ModItems.decoration("lavender_wall_frame"), frame.south(), Direction.NORTH);
        use(helper, frame, player);
        check(helper, at(helper, frame).getValue(DecorBlock.STYLE) == 1, "picture didn't change");
        place(helper, player, ModItems.decoration("fairy_fairy_door"), door.south(), Direction.NORTH);
        use(helper, door, player);
        check(helper, at(helper, door).getValue(DecorBlock.OPEN), "fairy door didn't open on a knock");
        place(helper, player, ModItems.decoration("vintage_mini_fountain"), fountain.below(), Direction.UP);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE));
        use(helper, fountain, player);
        ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
        check(helper, held.is(Items.POTION) && PotionUtils.getPotion(held) == Potions.WATER, "fountain didn't fill the bottle");
        helper.runAfterDelay(70, () -> {
            check(helper, !at(helper, door).getValue(DecorBlock.OPEN), "fairy door didn't close again");
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE)
    public static void collectionRemembersDecorations(GameTestHelper helper) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(),
                new GameProfile(UUID.nameUUIDFromBytes("cottage-collector".getBytes()), "cottage_collector"));
        ItemStack teapot = new ItemStack(ModItems.decoration("strawberry_teapot"));
        check(helper, CollectionData.collect(player, teapot, false), "first teapot should be new");
        check(helper, !CollectionData.collect(player, teapot, false), "second teapot shouldn't be new");
        check(helper, CollectionData.has(player, "strawberry_teapot"), "teapot wasn't remembered");
        check(helper, !CollectionData.collect(player, new ItemStack(Items.DIRT), false), "dirt isn't a decoration");
        check(helper, !CollectionData.completed(player, DecorSet.STRAWBERRY_COTTAGE), "one teapot isn't the whole set");
        for (Collectibles.Entry entry : Collectibles.inSet(DecorSet.STRAWBERRY_COTTAGE)) {
            CollectionData.collect(player, new ItemStack(entry.item()), false);
        }
        check(helper, CollectionData.completed(player, DecorSet.STRAWBERRY_COTTAGE), "the strawberry set should be complete");
        helper.succeed();
    }
}
