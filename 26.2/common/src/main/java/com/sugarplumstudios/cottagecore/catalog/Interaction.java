package com.sugarplumstudios.cottagecore.catalog;

import java.util.Locale;

/** What happens when a decoration is right-clicked. */
public enum Interaction {
    NONE,
    TOGGLE,
    READ_BOOK,
    COMPLIMENT,
    CHIRP,
    CHIME,
    WATER_PLANTS,
    FILL_BOTTLE,
    SHAKE,
    STEAM,
    KNOCK,
    CYCLE_STYLE,
    CYCLE_SEASON,
    TELL_TIME,
    OPEN_STORAGE;

    public String translationKey() {
        return "tooltip.cottagecoreliving.interaction." + name().toLowerCase(Locale.ROOT);
    }
}
