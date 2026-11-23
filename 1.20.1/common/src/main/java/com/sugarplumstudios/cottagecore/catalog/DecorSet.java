package com.sugarplumstudios.cottagecore.catalog;

import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

/** The five themed decorative sets. */
public enum DecorSet implements StringRepresentable {
    STRAWBERRY_COTTAGE("strawberry", 0xE5484D, 0xFFF1EE),
    LAVENDER_HOME("lavender", 0x8364C2, 0xF6F2FD),
    PINK_GARDEN("pink", 0xD9668E, 0xFFF2F6),
    FAIRYCORE("fairy", 0x5FAE8C, 0xF1FBF6),
    VINTAGE_COTTAGE("vintage", 0x5D8576, 0xF4F1E7);

    private final String id;
    private final int color;
    private final int paleColor;

    DecorSet(String id, int color, int paleColor) {
        this.id = id;
        this.color = color;
        this.paleColor = paleColor;
    }

    public String id() {
        return id;
    }

    /** The set's signature colour (RGB). */
    public int color() {
        return color;
    }

    public int paleColor() {
        return paleColor;
    }

    public String translationKey() {
        return "set.cottagecoreliving." + id;
    }

    public Component displayName() {
        return Component.translatable(translationKey());
    }

    @Override
    public String getSerializedName() {
        return id;
    }

    @Nullable
    public static DecorSet byId(String id) {
        for (DecorSet set : values()) {
            if (set.id.equals(id)) {
                return set;
            }
        }
        return null;
    }
}
