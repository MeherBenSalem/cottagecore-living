package com.sugarplumstudios.cottagecore.event;

import com.sugarplumstudios.cottagecore.CottagecoreLiving;
import com.sugarplumstudios.cottagecore.collection.CollectionData;
import com.sugarplumstudios.cottagecore.config.CottageConfig;
import com.sugarplumstudios.cottagecore.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/** Joining (book, recipes) and the ways a decoration enters a player's collection. */
@Mod.EventBusSubscriber(modid = CottagecoreLiving.MOD_ID)
public final class CommonEvents {
    private CommonEvents() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (CottageConfig.COMMON.unlockAllRecipes.get()) {
            List<Recipe<?>> ours = player.server.getRecipeManager().getRecipes().stream()
                    .filter(recipe -> CottagecoreLiving.MOD_ID.equals(recipe.getId().getNamespace()))
                    .toList();
            player.awardRecipes(ours);
        }
        if (CottageConfig.COMMON.giveBookOnFirstJoin.get() && CollectionData.firstBook(player)) {
            ItemStack book = new ItemStack(ModItems.COLLECTION_BOOK.get());
            if (!player.getInventory().add(book)) {
                player.drop(book, false);
            }
        }
        CollectionData.scanInventory(player);
    }

    @SubscribeEvent
    public static void onCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CollectionData.collect(player, event.getCrafting(), true);
        }
    }

    @SubscribeEvent
    public static void onPickup(PlayerEvent.ItemPickupEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CollectionData.collect(player, event.getStack(), true);
        }
    }

    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CollectionData.collect(player, new ItemStack(event.getPlacedBlock().getBlock().asItem()), true);
        }
    }
}
