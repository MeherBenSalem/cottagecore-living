package com.sugarplumstudios.cottagecore.catalog;

import java.util.Locale;

/** The three collections a decoration belongs to (also the creative tabs and book chapters). */
public enum Category {
    INTERIOR,
    GARDEN,
    SEASONAL;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "category.cottagecoreliving." + id();
    }
}
