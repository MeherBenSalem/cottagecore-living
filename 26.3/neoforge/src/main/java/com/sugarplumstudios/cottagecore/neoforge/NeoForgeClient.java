package com.sugarplumstudios.cottagecore.neoforge;

import com.sugarplumstudios.cottagecore.client.WaterTint;
import com.sugarplumstudios.cottagecore.config.CottageConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import java.util.List;

/** Client setup: the client config and the water tint of fountains and birdbaths (items tint in their JSON). */
final class NeoForgeClient {
    private NeoForgeClient() {
    }

    static void init(IEventBus modBus) {
        CottageConfig.loadClient();
        modBus.addListener((RegisterColorHandlersEvent.BlockTintSources event) ->
                event.register(List.of(WaterTint.INSTANCE), WaterTint.watery()));
    }
}
