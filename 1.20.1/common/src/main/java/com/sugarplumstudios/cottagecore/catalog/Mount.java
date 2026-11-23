package com.sugarplumstudios.cottagecore.catalog;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/** Where a decoration is attached: standing on a surface, hung on a wall or hung from above. */
public enum Mount implements StringRepresentable {
    FLOOR,
    WALL,
    CEILING;

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
