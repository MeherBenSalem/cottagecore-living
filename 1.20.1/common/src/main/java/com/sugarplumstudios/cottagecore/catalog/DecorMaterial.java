package com.sugarplumstudios.cottagecore.catalog;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

/** What a decoration is made of: sets its sounds, map colour and how quickly it breaks. */
public enum DecorMaterial {
    WOOD(SoundType.WOOD, MapColor.WOOD, 0.8F, true),
    STONE(SoundType.STONE, MapColor.STONE, 1.0F, false),
    CERAMIC(SoundType.DECORATED_POT, MapColor.TERRACOTTA_WHITE, 0.6F, false),
    GLASS(SoundType.GLASS, MapColor.NONE, 0.4F, false),
    CLOTH(SoundType.WOOL, MapColor.WOOL, 0.4F, true),
    METAL(SoundType.CHAIN, MapColor.METAL, 0.8F, false),
    PLANT(SoundType.AZALEA_LEAVES, MapColor.PLANT, 0.2F, true),
    CANDLE(SoundType.CANDLE, MapColor.SAND, 0.2F, false),
    PAPER(SoundType.CHISELED_BOOKSHELF, MapColor.WOOD, 0.5F, true),
    SNOW(SoundType.SNOW, MapColor.SNOW, 0.3F, false),
    LANTERN(SoundType.LANTERN, MapColor.METAL, 0.8F, false);

    private final SoundType sound;
    private final MapColor mapColor;
    private final float strength;
    private final boolean flammable;

    DecorMaterial(SoundType sound, MapColor mapColor, float strength, boolean flammable) {
        this.sound = sound;
        this.mapColor = mapColor;
        this.strength = strength;
        this.flammable = flammable;
    }

    public SoundType sound() {
        return sound;
    }

    public MapColor mapColor() {
        return mapColor;
    }

    public float strength() {
        return strength;
    }

    public boolean flammable() {
        return flammable;
    }
}
