package com.sugarplumstudios.cottagecore.forge;

import com.sugarplumstudios.cottagecore.client.WaterTint;
import com.sugarplumstudios.cottagecore.config.CottageConfig;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/** Client setup: the client config and the water tint of fountains and birdbaths. */
final class ForgeClient {
    private ForgeClient() {
    }

    static void init(IEventBus modBus) {
        CottageConfig.loadClient();
        modBus.addListener((RegisterColorHandlersEvent.Block event) ->
                event.register((state, level, pos, tint) -> WaterTint.block(level, pos), WaterTint.watery()));
        modBus.addListener((RegisterColorHandlersEvent.Item event) ->
                event.register((stack, tint) -> WaterTint.item(tint), WaterTint.watery()));
    }
}
