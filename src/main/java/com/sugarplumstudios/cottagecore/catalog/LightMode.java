package com.sugarplumstudios.cottagecore.catalog;

/** How a decoration gives off light. */
public enum LightMode {
    /** No light. */
    NONE,
    /** Always glows (firefly jars, twinkling garlands). */
    ALWAYS,
    /** Lit with flint and steel, snuffed with an empty hand; the level is per candle. */
    CANDLE,
    /** Switched on and off with a right-click (lanterns). */
    SWITCH
}
