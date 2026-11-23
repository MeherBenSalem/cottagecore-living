package com.sugarplumstudios.cottagecore.neoforge;

import com.sugarplumstudios.cottagecore.client.WaterTint;
import com.sugarplumstudios.cottagecore.config.CottageConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

/** Client setup: the client config and the water tint of fountains and birdbaths. */
final class NeoForgeClient {
    private NeoForgeClient() {
    }

    static void init(IEventBus modBus) {
        CottageConfig.loadClient();
        modBus.addListener((RegisterColorHandlersEvent.Block event) ->
                event.register((state, level, pos, tint) -> WaterTint.block(level, pos), WaterTint.watery()));
        modBus.addListener((RegisterColorHandlersEvent.Item event) ->
                event.register((stack, tint) -> WaterTint.item(tint), WaterTint.watery()));
    }
}
