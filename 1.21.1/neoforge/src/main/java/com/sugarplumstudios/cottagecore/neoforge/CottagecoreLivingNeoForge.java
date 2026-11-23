package com.sugarplumstudios.cottagecore.neoforge;

import com.sugarplumstudios.cottagecore.CottagecoreLiving;
import com.sugarplumstudios.cottagecore.event.CommonEvents;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@Mod(CottagecoreLiving.MOD_ID)
public final class CottagecoreLivingNeoForge {
    public CottagecoreLivingNeoForge(IEventBus modBus) {
        CottagecoreLiving.init();
        NeoForgePlatform.registerAll(modBus);
        modBus.addListener(NeoForgeNetwork::register);

        NeoForge.EVENT_BUS.addListener(CottagecoreLivingNeoForge::onLogin);
        NeoForge.EVENT_BUS.addListener(CottagecoreLivingNeoForge::onCrafted);
        NeoForge.EVENT_BUS.addListener(CottagecoreLivingNeoForge::onPickup);
        NeoForge.EVENT_BUS.addListener(CottagecoreLivingNeoForge::onPlace);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            NeoForgeClient.init(modBus);
        }
    }

    private static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CommonEvents.onLogin(player);
        }
    }

    private static void onCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CommonEvents.onCrafted(player, event.getCrafting());
        }
    }

    private static void onPickup(ItemEntityPickupEvent.Post event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            CommonEvents.onPickup(player, event.getOriginalStack());
        }
    }

    private static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CommonEvents.onPlace(player, event.getPlacedBlock());
        }
    }
}
