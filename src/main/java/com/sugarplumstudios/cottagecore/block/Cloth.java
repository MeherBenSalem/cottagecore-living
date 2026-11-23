package com.sugarplumstudios.cottagecore.block;

import com.sugarplumstudios.cottagecore.catalog.DecorSet;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

/** The tablecloth on a cottage table, if any. */
public enum Cloth implements StringRepresentable {
    NONE(null),
    STRAWBERRY(DecorSet.STRAWBERRY_COTTAGE),
    LAVENDER(DecorSet.LAVENDER_HOME),
    PINK(DecorSet.PINK_GARDEN),
    FAIRY(DecorSet.FAIRYCORE),
    VINTAGE(DecorSet.VINTAGE_COTTAGE);

    @Nullable
    private final DecorSet set;

    Cloth(@Nullable DecorSet set) {
        this.set = set;
    }

    @Nullable
    public DecorSet set() {
        return set;
    }

    public static Cloth of(DecorSet set) {
        for (Cloth cloth : values()) {
            if (cloth.set == set) {
                return cloth;
            }
        }
        return NONE;
    }

    @Override
    public String getSerializedName() {
        return set == null ? "none" : set.id();
    }
}
