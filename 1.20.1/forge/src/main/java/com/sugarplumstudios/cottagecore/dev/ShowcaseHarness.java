package com.sugarplumstudios.cottagecore.dev;

import com.sugarplumstudios.cottagecore.CottagecoreLiving;
import com.sugarplumstudios.cottagecore.block.Cloth;
import com.sugarplumstudios.cottagecore.block.DecorBlock;
import com.sugarplumstudios.cottagecore.block.SeasonalDecorBlock;
import com.sugarplumstudios.cottagecore.block.TableDecorBlock;
import com.sugarplumstudios.cottagecore.catalog.DecorSet;
import com.sugarplumstudios.cottagecore.catalog.Mount;
import com.sugarplumstudios.cottagecore.catalog.Season;
import com.sugarplumstudios.cottagecore.client.screen.CollectionBookScreen;
import com.sugarplumstudios.cottagecore.collection.Collectibles;
import com.sugarplumstudios.cottagecore.config.CottageConfig;
import com.sugarplumstudios.cottagecore.network.OpenBookPacket;
import com.sugarplumstudios.cottagecore.registry.ModBlocks;
import com.sugarplumstudios.cottagecore.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Development only (stripped from the release jar). Launch the client with
 * {@code ./gradlew runClient -Pshowcase} and it builds a flat creative world,
 * decorates a parlour, a garden, a seasons corner and a set lineup, screenshots
 * each one plus the collection book into {@code runs/client/screenshots}, then quits.
 */
@Mod.EventBusSubscriber(modid = CottagecoreLiving.MOD_ID, value = Dist.CLIENT)
public final class ShowcaseHarness {
    private static final boolean ENABLED = Boolean.getBoolean("cottagecoreliving.showcase");
    private static final int Y = -60;
    private static final Direction S = Direction.SOUTH;

    private record Shot(String name, double x, double y, double z, float yaw, float pitch) {
    }

    private static boolean worldRequested;
    private static boolean built;
    private static int tick;
    private static final List<Shot> SHOTS = new ArrayList<>();
    private static final List<BlockPos> PLACED = new ArrayList<>();
    private static int shotIndex;
    private static int bookStage;

    private ShowcaseHarness() {
    }

    private static void createWorld(Minecraft mc) {
        worldRequested = true;
        mc.getWindow().setWindowed(1280, 720);
        mc.options.hideGui = true;
        LevelSettings settings = new LevelSettings("Cottage Showcase", GameType.CREATIVE, false, Difficulty.PEACEFUL, true,
                new GameRules(), WorldDataConfiguration.DEFAULT);
        mc.createWorldOpenFlows().createFreshLevel("cottage_showcase_" + System.currentTimeMillis(), settings,
                new WorldOptions(1L, false, false),
                access -> access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions());
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (!ENABLED || event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        // on a fresh run folder the first menu is the accessibility onboarding screen, not the title screen
        if (!worldRequested && mc.level == null && mc.screen != null && mc.getOverlay() == null) {
            mc.options.onboardAccessibility = false;
            mc.options.save();
            createWorld(mc);
            return;
        }
        if (mc.player == null || mc.level == null || mc.getSingleplayerServer() == null) {
            return;
        }
        tick++;
        if (tick == 40 && !built) {
            built = true;
            mc.getSingleplayerServer().execute(() -> build(mc.getSingleplayerServer().overworld()));
            mc.player.getAbilities().flying = true;
            return;
        }
        if (tick < 140) {
            return;
        }
        int phase = tick - 140;
        if (shotIndex < SHOTS.size()) {
            Shot shot = SHOTS.get(shotIndex);
            if (phase % 70 == 0) {
                mc.player.getAbilities().flying = true;
                mc.player.moveTo(shot.x, shot.y, shot.z, shot.yaw, shot.pitch);
                mc.player.setDeltaMovement(0, 0, 0);
            } else if (phase % 70 == 69) {
                Screenshot.grab(mc.gameDirectory, "cottage_" + shot.name + ".png", mc.getMainRenderTarget(), msg -> {
                });
                shotIndex++;
            }
            return;
        }
        bookStage++;
        if (bookStage == 1) {
            GLFW.glfwSetCursorPos(mc.getWindow().getWindow(), 2, 2);
            List<String> ids = Collectibles.all().stream().map(Collectibles.Entry::id).limit(130).toList();
            mc.setScreen(new CollectionBookScreen(new OpenBookPacket(ids, Season.SPRING,
                    CottageConfig.SeasonMode.CALENDAR, true)));
        } else if (bookStage == 10 && mc.screen instanceof CollectionBookScreen book) {
            book.previewSelect(1, ModItems.decoration("strawberry_teapot"));
        } else if (bookStage == 30) {
            Screenshot.grab(mc.gameDirectory, "cottage_book.png", mc.getMainRenderTarget(), msg -> {
            });
        } else if (bookStage == 35 && mc.screen instanceof CollectionBookScreen book) {
            book.previewSelect(2, ModItems.decoration("fairy_mini_fountain"));
        } else if (bookStage == 55) {
            Screenshot.grab(mc.gameDirectory, "cottage_book_garden.png", mc.getMainRenderTarget(), msg -> {
            });
        } else if (bookStage == 60 && mc.screen instanceof CollectionBookScreen book) {
            book.previewSet(DecorSet.FAIRYCORE);
        } else if (bookStage == 80) {
            Screenshot.grab(mc.gameDirectory, "cottage_book_sets.png", mc.getMainRenderTarget(), msg -> {
            });
        } else if (bookStage == 95) {
            mc.stop();
        }
    }

    // ------------------------------------------------------------------ placing

    private static BlockState d(String id, Direction facing) {
        BlockState state = ModBlocks.get(id).defaultBlockState();
        return state.hasProperty(DecorBlock.FACING) ? state.setValue(DecorBlock.FACING, facing) : state;
    }

    private static BlockState d(String id) {
        return d(id, S);
    }

    private static BlockState mount(BlockState state, Mount mount) {
        EnumProperty<Mount> prop = ((DecorBlock) state.getBlock()).mountProperty();
        return prop == null ? state : state.setValue(prop, mount);
    }

    private static void put(ServerLevel level, int x, int y, int z, BlockState state) {
        BlockPos pos = new BlockPos(x, y, z);
        level.setBlock(pos, state, Block.UPDATE_ALL);
        PLACED.add(pos);
    }

    private static void block(ServerLevel level, int x, int y, int z, Block block) {
        level.setBlock(new BlockPos(x, y, z), block.defaultBlockState(), Block.UPDATE_ALL);
    }

    /** Lets connecting pieces, curtains and low decorations look at their finished neighbours. */
    private static void settle(ServerLevel level) {
        for (BlockPos pos : PLACED) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof DecorBlock) {
                level.setBlock(pos, Block.updateFromNeighbourShapes(state, level, pos), Block.UPDATE_ALL);
            }
        }
    }

    private static void room(ServerLevel level, int x0, int z0, int w, int d, Block floor, Block wall) {
        for (int x = x0; x < x0 + w; x++) {
            for (int z = z0; z < z0 + d; z++) {
                block(level, x, Y - 1, z, floor);
            }
            for (int y = Y; y < Y + 4; y++) {
                block(level, x, y, z0 - 1, wall);
            }
        }
        for (int z = z0 - 1; z < z0 + d; z++) {
            for (int y = Y; y < Y + 4; y++) {
                block(level, x0 - 1, y, z, wall);
            }
        }
    }

    // ------------------------------------------------------------------ scenes

    private static void build(ServerLevel level) {
        level.setDayTime(6000);
        level.setWeatherParameters(6000, 0, false, false);
        level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, level.getServer());
        parlour(level);
        garden(level);
        seasons(level);
        sets(level);
        walls(level);
        settle(level);
    }

    private static void parlour(ServerLevel level) {
        room(level, 0, 0, 9, 7, Blocks.SPRUCE_PLANKS, Blocks.BIRCH_PLANKS);
        for (int x = 4; x <= 5; x++) {
            for (int y = Y + 1; y <= Y + 2; y++) {
                block(level, x, y, -1, Blocks.GLASS);
            }
        }
        put(level, 4, Y + 2, 0, d("pink_curtains"));
        put(level, 4, Y + 1, 0, d("pink_curtains"));
        put(level, 5, Y + 2, 0, d("pink_curtains").setValue(DecorBlock.OPEN, true));
        put(level, 5, Y + 1, 0, d("pink_curtains").setValue(DecorBlock.OPEN, true));
        put(level, 1, Y, 1, d("oak_cottage_table").setValue(TableDecorBlock.CLOTH, Cloth.STRAWBERRY));
        put(level, 2, Y, 1, d("oak_cottage_table").setValue(TableDecorBlock.CLOTH, Cloth.STRAWBERRY));
        put(level, 1, Y + 1, 1, d("strawberry_teapot"));
        put(level, 2, Y + 1, 1, d("strawberry_teacups"));
        put(level, 7, Y, 1, d("birch_cottage_table").setValue(TableDecorBlock.CLOTH, Cloth.VINTAGE));
        put(level, 7, Y + 1, 1, d("vintage_mantel_clock"));
        put(level, 0, Y + 2, 0, d("strawberry_embroidery_hoop"));
        put(level, 1, Y + 2, 0, d("strawberry_wall_shelf"));
        put(level, 2, Y + 2, 0, d("pink_wall_clock"));
        put(level, 3, Y + 2, 0, d("lavender_wall_frame").setValue(DecorBlock.STYLE, 3));
        put(level, 6, Y + 2, 0, d("vintage_wall_plates"));
        put(level, 7, Y + 2, 0, d("vintage_wall_mirror"));
        put(level, 8, Y + 1, 0, d("fairy_candle_sconce").setValue(DecorBlock.LIT, true));
        for (int x = 0; x <= 3; x++) {
            put(level, x, Y + 3, 0, d("strawberry_bunting"));
        }
        put(level, 0, Y + 2, 2, d("lavender_dried_flowers", Direction.EAST));
        put(level, 0, Y + 2, 4, d("fairy_cuckoo_clock", Direction.EAST));
        put(level, 0, Y, 3, d("lavender_book_row", Direction.EAST));
        put(level, 3, Y, 2, d("lavender_stacked_books").setValue(DecorBlock.COUNT, 3));
        put(level, 6, Y, 2, d("vintage_pillar_candles").setValue(DecorBlock.COUNT, 3).setValue(DecorBlock.LIT, true));
        for (int x = 2; x <= 4; x++) {
            for (int z = 4; z <= 5; z++) {
                block(level, x, Y, z, Blocks.PINK_CARPET);
            }
        }
        put(level, 3, Y + 1, 4, d("fairy_teapot"));
        put(level, 2, Y + 1, 5, d("basket_of_yarn"));
        put(level, 5, Y, 4, d("basket_of_apples"));
        put(level, 8, Y, 3, d("pink_flower_vase"));
        put(level, 8, Y, 5, d("firefly_jar"));
        put(level, 6, Y, 5, d("pink_jewelry_box").setValue(DecorBlock.OPEN, true));
        SHOTS.add(new Shot("parlour", 9.5, Y + 2.2, 8.5, 135, 22));
        SHOTS.add(new Shot("parlour_closeup", 5.2, Y + 1.9, 4.6, 150, 20));
    }

    private static void garden(ServerLevel level) {
        int gx = 14;
        for (int x = gx - 1; x < gx + 12; x++) {
            for (int z = -1; z < 9; z++) {
                block(level, x, Y - 1, z, Blocks.GRASS_BLOCK);
            }
            block(level, x, Y, -1, Blocks.OAK_FENCE);
        }
        put(level, gx + 2, Y + 1, -1, d("pink_birdhouse"));
        for (int y = Y; y < Y + 4; y++) {
            block(level, gx + 10, y, -1, Blocks.OAK_LOG);
        }
        put(level, gx + 10, Y, 0, d("fairy_fairy_door").setValue(DecorBlock.OPEN, true));
        for (int y = Y; y < Y + 3; y++) {
            block(level, gx + 5, y, 0, Blocks.OAK_FENCE);
            block(level, gx + 8, y, 0, Blocks.OAK_FENCE);
        }
        for (int x = gx + 5; x <= gx + 8; x++) {
            block(level, x, Y + 3, 0, Blocks.OAK_PLANKS);
        }
        put(level, gx + 6, Y + 2, 0, mount(d("lavender_wind_chime"), Mount.CEILING));
        put(level, gx + 7, Y + 2, 0, d("fairy_hanging_planter"));
        put(level, gx + 3, Y, 2, d("fairy_mini_fountain"));
        put(level, gx + 1, Y, 2, d("vintage_garden_gnome"));
        put(level, gx, Y, 4, d("strawberry_toadstools"));
        put(level, gx + 9, Y, 2, d("pink_garden_sign").setValue(DecorBlock.STYLE, 2));
        for (int x = gx + 3; x <= gx + 5; x++) {
            put(level, x, Y, 5, d("mossy_stepping_stones"));
        }
        put(level, gx + 7, Y, 3, mount(d("strawberry_garden_lantern"), Mount.FLOOR).setValue(DecorBlock.LIT, true));
        put(level, gx + 5, Y, 3, d("strawberry_watering_can"));
        put(level, gx + 10, Y, 3, d("lavender_flower_wheelbarrow"));
        put(level, gx + 6, Y, 6, d("pink_painted_rock"));
        for (int x = gx; x <= gx + 1; x++) {
            for (int z = 6; z <= 7; z++) {
                put(level, x, Y, z, d("strawberry_picnic_blanket"));
            }
        }
        put(level, gx, Y + 1, 6, d("picnic_basket").setValue(DecorBlock.OPEN, true));
        put(level, gx + 1, Y + 1, 7, d("strawberry_sun_hat"));
        put(level, gx + 1, Y + 1, 6, d("lemonade_pitcher"));
        SHOTS.add(new Shot("garden", gx + 11.5, Y + 2.6, 10.5, 135, 22));
    }

    private static void seasons(ServerLevel level) {
        int sx = 30;
        room(level, sx, 0, 14, 7, Blocks.OAK_PLANKS, Blocks.SPRUCE_PLANKS);
        Season[] all = Season.values();
        for (int i = 0; i < all.length; i++) {
            int x = sx + 1 + i * 3;
            BlockState seasonal = d("seasonal_tree").setValue(SeasonalDecorBlock.SEASON, all[i]).setValue(SeasonalDecorBlock.PINNED, true);
            put(level, x, Y, 1, seasonal);
            put(level, x, Y + 2, 0, d("seasonal_wreath").setValue(SeasonalDecorBlock.SEASON, all[i]).setValue(SeasonalDecorBlock.PINNED, true));
            put(level, x, Y + 3, 0, d("seasonal_garland").setValue(SeasonalDecorBlock.SEASON, all[i]).setValue(SeasonalDecorBlock.PINNED, true));
            put(level, x + 1, Y + 3, 0, d("seasonal_garland").setValue(SeasonalDecorBlock.SEASON, all[i]).setValue(SeasonalDecorBlock.PINNED, true));
            put(level, x + 1, Y, 3, d("oak_cottage_table"));
            put(level, x + 1, Y + 1, 3, d("seasonal_centerpiece").setValue(SeasonalDecorBlock.SEASON, all[i]).setValue(SeasonalDecorBlock.PINNED, true));
            put(level, x, Y, 5, d("seasonal_doormat").setValue(SeasonalDecorBlock.SEASON, all[i]).setValue(SeasonalDecorBlock.PINNED, true));
        }
        put(level, sx, Y, 3, d("blossom_branch_vase"));
        put(level, sx + 13, Y, 1, d("mini_snowman"));
        put(level, sx + 13, Y, 3, d("orange_leaf_pile").setValue(DecorBlock.COUNT, 3));
        put(level, sx + 12, Y, 5, d("pumpkin_stack").setValue(DecorBlock.COUNT, 3));
        put(level, sx + 13, Y, 5, d("hot_cocoa").setValue(DecorBlock.COUNT, 3));
        put(level, sx + 2, Y + 2, 0, d("blossom_wreath"));
        put(level, sx + 11, Y + 2, 0, d("winter_wreath"));
        SHOTS.add(new Shot("seasons", sx + 7, Y + 3.4, 10.5, 180, 24));
    }

    private static void sets(ServerLevel level) {
        String[] lineup = {"teapot", "flower_vase", "pillar_candles", "candle_jar", "jewelry_box", "vanity_mirror",
                "birdhouse", "watering_can", "garden_gnome", "toadstools", "garden_lantern", "flower_basket", "sun_hat",
                "mini_fountain", "trinket_box", "bud_vase"};
        int x0 = 50;
        DecorSet[] sets = DecorSet.values();
        for (int x = x0 - 1; x < x0 + lineup.length + 1; x++) {
            for (int z = -1; z < sets.length * 2 + 1; z++) {
                block(level, x, Y - 1, z, Blocks.SMOOTH_QUARTZ);
            }
        }
        for (int r = 0; r < sets.length; r++) {
            for (int c = 0; c < lineup.length; c++) {
                BlockState state = d(sets[r].id() + "_" + lineup[c]);
                if (state.hasProperty(DecorBlock.LIT)) {
                    state = state.setValue(DecorBlock.LIT, true);
                }
                if (state.getBlock() instanceof DecorBlock decor && decor.mountProperty() != null) {
                    state = mount(state, Mount.FLOOR);
                }
                put(level, x0 + c, Y, r * 2, state);
            }
        }
        SHOTS.add(new Shot("sets", x0 + lineup.length / 2.0, Y + 6.5, sets.length * 2 + 5.5, 180, 42));
    }

    private static void walls(ServerLevel level) {
        String[] lineup = {"embroidery_hoop", "wall_plates", "dried_flowers", "wall_shelf", "wall_clock", "cuckoo_clock",
                "wall_mirror", "wall_frame", "candle_sconce", "curtains", "fairy_door", "window_box", "bunting"};
        int x0 = 72;
        for (int x = x0 - 1; x < x0 + lineup.length + 1; x++) {
            for (int z = 0; z < 6; z++) {
                block(level, x, Y - 1, z, Blocks.SMOOTH_QUARTZ);
            }
            for (int y = Y; y < Y + 5; y++) {
                block(level, x, y, -1, Blocks.WHITE_TERRACOTTA);
            }
        }
        DecorSet[] rows = {DecorSet.PINK_GARDEN, DecorSet.FAIRYCORE};
        for (int r = 0; r < rows.length; r++) {
            for (int c = 0; c < lineup.length; c++) {
                BlockState state = d(rows[r].id() + "_" + lineup[c]);
                if (state.getBlock() instanceof DecorBlock decor && decor.mountProperty() != null) {
                    state = mount(state, Mount.WALL);
                }
                if (state.hasProperty(DecorBlock.LIT)) {
                    state = state.setValue(DecorBlock.LIT, true);
                }
                put(level, x0 + c, r == 0 ? Y + 3 : Y + 1, 0, state);
            }
        }
        SHOTS.add(new Shot("walls", x0 + lineup.length / 2.0, Y + 2.5, 9.5, 180, 8));
    }
}
