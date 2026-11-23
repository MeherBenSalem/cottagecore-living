package com.sugarplumstudios.cottagecore.event;

import com.sugarplumstudios.cottagecore.CottagecoreLiving;
import com.sugarplumstudios.cottagecore.collection.CollectionData;
import com.sugarplumstudios.cottagecore.config.CottageConfig;
import com.sugarplumstudios.cottagecore.registry.ModItems;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Joining (book, recipes) and the ways a decoration enters a player's collection. The loaders call these from
 * their own events: Forge and NeoForge from the login, crafted, pickup and place events, Fabric from the join
 * event and a once-a-second inventory check (Fabric API has no crafted, pickup or place events).
 */
public final class CommonEvents {
    private static int tick;

    private CommonEvents() {
    }

    public static void onLogin(ServerPlayer player) {
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

    public static void onCrafted(ServerPlayer player, ItemStack crafted) {
        CollectionData.collect(player, crafted, true);
    }

    public static void onPickup(ServerPlayer player, ItemStack stack) {
        CollectionData.collect(player, stack, true);
    }

    public static void onPlace(ServerPlayer player, BlockState placed) {
        CollectionData.collect(player, new ItemStack(placed.getBlock().asItem()), true);
    }

    /** Fabric only: once a second, records (and announces) decorations that reached a player's inventory. */
    public static void onServerTick(MinecraftServer server) {
        if (++tick % 20 != 0) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            for (ItemStack stack : player.getInventory().items) {
                CollectionData.collect(player, stack, true);
            }
            for (ItemStack stack : player.getInventory().offhand) {
                CollectionData.collect(player, stack, true);
            }
        }
    }
}
