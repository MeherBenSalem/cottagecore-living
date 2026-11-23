package com.sugarplumstudios.cottagecore.config;

import com.sugarplumstudios.cottagecore.catalog.Season;
import com.sugarplumstudios.cottagecore.platform.Services;

/**
 * The mod's two config files, {@code config/cottagecoreliving-common.toml} (server rules) and
 * {@code config/cottagecoreliving-client.toml}. Same layout and keys as the Forge 1.0 files, on every loader and version.
 */
public final class CottageConfig {
    private static final TomlConfig.Builder COMMON_BUILDER = new TomlConfig.Builder();
    public static final Common COMMON = new Common(COMMON_BUILDER);
    private static final TomlConfig COMMON_FILE = COMMON_BUILDER.build("cottagecoreliving-common.toml");
    private static final TomlConfig.Builder CLIENT_BUILDER = new TomlConfig.Builder();
    public static final Client CLIENT = new Client(CLIENT_BUILDER);
    private static final TomlConfig CLIENT_FILE = CLIENT_BUILDER.build("cottagecoreliving-client.toml");
    private static boolean clientLoaded;

    private CottageConfig() {
    }

    /** Server rules; read at mod start. */
    public static void loadCommon() {
        COMMON_FILE.load(Services.PLATFORM.configDir());
    }

    /** Client options; read when the client starts. */
    public static void loadClient() {
        CLIENT_FILE.load(Services.PLATFORM.configDir());
        clientLoaded = true;
    }

    /** Client config can be asked for on a server (e.g. by tooltip-reading mods), where it never loads. */
    public static boolean detailedTooltips() {
        return !clientLoaded || CLIENT.detailedTooltips.get();
    }

    /** How the world's season is chosen for seasonal decorations. */
    public enum SeasonMode {
        /** From the real-world calendar of the server. */
        CALENDAR,
        /** Turns every few in-game days. */
        CYCLE,
        /** Always the configured season. */
        FIXED
    }

    public static final class Common {
        public final TomlConfig.EnumValue<SeasonMode> seasonMode;
        public final TomlConfig.EnumValue<Season> fixedSeason;
        public final TomlConfig.IntValue daysPerSeason;
        public final TomlConfig.BooleanValue southernHemisphere;

        public final TomlConfig.BooleanValue wateringCanGrowsCrops;
        public final TomlConfig.BooleanValue fountainsFillBottles;
        public final TomlConfig.BooleanValue cozyEffects;
        public final TomlConfig.BooleanValue cuckooCalls;
        public final TomlConfig.BooleanValue fairyGifts;

        public final TomlConfig.BooleanValue giveBookOnFirstJoin;
        public final TomlConfig.BooleanValue collectionMessages;
        public final TomlConfig.BooleanValue deliveryInSurvival;
        public final TomlConfig.BooleanValue unlockAllRecipes;

        Common(TomlConfig.Builder builder) {
            builder.comment("Seasonal decorations (wreaths, garlands, centerpieces, little trees and doormats)",
                    "follow the world's season unless a player picks one by right-clicking them.").push("seasons");
            seasonMode = builder
                    .comment("CALENDAR: the server's real-world date. CYCLE: turns every few in-game days.",
                            "FIXED: always the season set below.")
                    .defineEnum("mode", SeasonMode.CALENDAR);
            fixedSeason = builder
                    .comment("The season used when mode is FIXED.")
                    .defineEnum("fixedSeason", Season.SPRING);
            daysPerSeason = builder
                    .comment("In-game days per season when mode is CYCLE.")
                    .defineInRange("daysPerSeason", 7, 1, 365);
            southernHemisphere = builder
                    .comment("Flip the calendar seasons (December is summer) when mode is CALENDAR.")
                    .define("southernHemisphere", false);
            builder.pop();

            builder.comment("What decorations do when you use them").push("interactions");
            wateringCanGrowsCrops = builder
                    .comment("Watering cans give the crops in front of them a little growth tick (once a second at most).")
                    .define("wateringCanGrowsCrops", true);
            fountainsFillBottles = builder
                    .comment("Mini fountains and birdbaths fill glass bottles with water.")
                    .define("fountainsFillBottles", true);
            cozyEffects = builder
                    .comment("Teapots, teacups and hot cocoa give a few seconds of Regeneration (every 30 seconds at most).")
                    .define("cozyEffects", true);
            cuckooCalls = builder
                    .comment("Cuckoo clocks call out every in-game hour.")
                    .define("cuckooCalls", true);
            fairyGifts = builder
                    .comment("Knocking on a fairy door sometimes earns a small gift.")
                    .define("fairyGifts", true);
            builder.pop();

            builder.comment("The Decoration Collection Book").push("collection");
            giveBookOnFirstJoin = builder
                    .comment("Give each player a Decoration Collection Book the first time they join.")
                    .define("giveBookOnFirstJoin", true);
            collectionMessages = builder
                    .comment("Tell players when they collect a decoration they haven't had before.")
                    .define("collectionMessages", true);
            deliveryInSurvival = builder
                    .comment("Let the book hand out decorations outside creative mode (a cheat; off by default).")
                    .define("deliveryInSurvival", false);
            unlockAllRecipes = builder
                    .comment("Unlock every decoration recipe in the recipe book when a player joins.")
                    .define("unlockAllRecipes", true);
            builder.pop();
        }
    }

    public static final class Client {
        public final TomlConfig.BooleanValue ambientParticles;
        public final TomlConfig.BooleanValue ambientSounds;
        public final TomlConfig.BooleanValue detailedTooltips;

        Client(TomlConfig.Builder builder) {
            builder.comment("Client-side presentation").push("display");
            ambientParticles = builder
                    .comment("Candle flames, fireflies, fountain splashes, falling petals and steam.")
                    .define("ambientParticles", true);
            ambientSounds = builder
                    .comment("Wind chimes ringing in the breeze, birds at the birdhouse, splashing fountains.")
                    .define("ambientSounds", true);
            detailedTooltips = builder
                    .comment("Describe what each decoration does in its tooltip.")
                    .define("detailedTooltips", true);
            builder.pop();
        }
    }
}
