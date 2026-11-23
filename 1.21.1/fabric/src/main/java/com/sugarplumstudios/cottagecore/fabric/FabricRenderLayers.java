package com.sugarplumstudios.cottagecore.fabric;

import java.util.Set;

/**
 * The decorations whose models ask for the translucent render type (glass jars, fountains, the snow globe). Every
 * other decoration renders cut out. Forge reads this from the models' render_type; Fabric needs it per block.
 */
final class FabricRenderLayers {
    static final Set<String> TRANSLUCENT = Set.of(
            "birdbath", "fairy_candle_jar", "fairy_mini_fountain", "firefly_jar", "jar_of_buttons", "jar_of_honey",
            "jar_of_ribbons", "jar_of_wildflowers", "lavender_candle_jar", "lavender_mini_fountain", "lemonade_pitcher",
            "pink_candle_jar", "pink_mini_fountain", "snow_globe", "strawberry_candle_jar", "strawberry_mini_fountain",
            "vintage_candle_jar", "vintage_mini_fountain");

    private FabricRenderLayers() {
    }
}
