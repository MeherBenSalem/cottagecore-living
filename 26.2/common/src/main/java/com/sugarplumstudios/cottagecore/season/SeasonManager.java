package com.sugarplumstudios.cottagecore.season;

import com.sugarplumstudios.cottagecore.catalog.Season;
import com.sugarplumstudios.cottagecore.config.CottageConfig;
import net.minecraft.world.level.Level;

import java.time.LocalDate;
import java.time.Month;

/** Decides which season the world is in, according to the common config. Server side. */
public final class SeasonManager {
    private SeasonManager() {
    }

    public static Season current(Level level) {
        CottageConfig.Common config = CottageConfig.COMMON;
        return switch (config.seasonMode.get()) {
            case FIXED -> config.fixedSeason.get();
            case CYCLE -> {
                long day = Math.max(0L, level.getOverworldClockTime() / 24000L);
                yield Season.values()[(int) ((day / config.daysPerSeason.get()) % 4L)];
            }
            case CALENDAR -> fromMonth(LocalDate.now().getMonth(), config.southernHemisphere.get());
        };
    }

    public static Season fromMonth(Month month, boolean southern) {
        Season season = switch (month) {
            case MARCH, APRIL, MAY -> Season.SPRING;
            case JUNE, JULY, AUGUST -> Season.SUMMER;
            case SEPTEMBER, OCTOBER, NOVEMBER -> Season.AUTUMN;
            default -> Season.WINTER;
        };
        return southern ? season.next().next() : season;
    }
}
