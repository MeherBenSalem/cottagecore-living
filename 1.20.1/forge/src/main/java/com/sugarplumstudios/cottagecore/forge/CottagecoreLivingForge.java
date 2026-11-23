package com.sugarplumstudios.cottagecore.forge;

import com.sugarplumstudios.cottagecore.CottagecoreLiving;
import com.sugarplumstudios.cottagecore.collection.CollectionData;
import com.sugarplumstudios.cottagecore.event.CommonEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.ArrayList;
import java.util.List;

@Mod(CottagecoreLiving.MOD_ID)
public final class CottagecoreLivingForge {
    /** Where Cottagecore Living 1.0 (Forge only) kept each player's collection. */
    private static final String OLD_COLLECTED = "cottagecoreliving_collected";
    private static final String OLD_RECEIVED_BOOK = "cottagecoreliving_received_book";

    public CottagecoreLivingForge() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        CottagecoreLiving.init();
        ForgePlatform.registerAll(modBus);
        ForgeNetwork.register();

        MinecraftForge.EVENT_BUS.addListener(CottagecoreLivingForge::onLogin);
        MinecraftForge.EVENT_BUS.addListener(CottagecoreLivingForge::onCrafted);
        MinecraftForge.EVENT_BUS.addListener(CottagecoreLivingForge::onPickup);
        MinecraftForge.EVENT_BUS.addListener(CottagecoreLivingForge::onPlace);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ForgeClient.init(modBus);
        }
    }

    private static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            migrate(player);
            CommonEvents.onLogin(player);
        }
    }

    private static void onCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CommonEvents.onCrafted(player, event.getCrafting());
        }
    }

    private static void onPickup(PlayerEvent.ItemPickupEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CommonEvents.onPickup(player, event.getStack());
        }
    }

    private static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CommonEvents.onPlace(player, event.getPlacedBlock());
        }
    }

    /** Moves a collection saved by 1.0 (in the player's persisted data) into the world's collection data. */
    private static void migrate(ServerPlayer player) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (!persisted.contains(OLD_COLLECTED) && !persisted.contains(OLD_RECEIVED_BOOK)) {
            return;
        }
        ListTag list = persisted.getList(OLD_COLLECTED, Tag.TAG_STRING);
        List<String> ids = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            ids.add(list.getString(i));
        }
        CollectionData.migrate(player, ids, persisted.getBoolean(OLD_RECEIVED_BOOK));
        persisted.remove(OLD_COLLECTED);
        persisted.remove(OLD_RECEIVED_BOOK);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }
}
