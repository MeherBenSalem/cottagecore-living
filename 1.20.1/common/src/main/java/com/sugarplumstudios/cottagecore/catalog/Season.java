package com.sugarplumstudios.cottagecore.catalog;

import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum Season implements StringRepresentable {
    SPRING,
    SUMMER,
    AUTUMN,
    WINTER;

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public Season next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public Component displayName() {
        return Component.translatable("season.cottagecoreliving." + getSerializedName());
    }
}
