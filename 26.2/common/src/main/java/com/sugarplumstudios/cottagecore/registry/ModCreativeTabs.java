package com.sugarplumstudios.cottagecore.registry;

import com.sugarplumstudios.cottagecore.catalog.Category;
import com.sugarplumstudios.cottagecore.collection.Collectibles;
import com.sugarplumstudios.cottagecore.platform.RegistryEntry;
import com.sugarplumstudios.cottagecore.platform.Services;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/** One creative tab per collection: Interior, Garden and Seasonal. */
public final class ModCreativeTabs {
    public static RegistryEntry<CreativeModeTab> INTERIOR;
    public static RegistryEntry<CreativeModeTab> GARDEN;
    public static RegistryEntry<CreativeModeTab> SEASONAL;

    private ModCreativeTabs() {
    }

    public static void init() {
        INTERIOR = tab("interior", "strawberry_teapot", Category.INTERIOR, null);
        GARDEN = tab("garden", "strawberry_birdhouse", Category.GARDEN, INTERIOR);
        SEASONAL = tab("seasonal", "blossom_branch_vase", Category.SEASONAL, GARDEN);
    }

    private static RegistryEntry<CreativeModeTab> tab(String name, String icon, Category category,
                                                      RegistryEntry<CreativeModeTab> after) {
        return Services.PLATFORM.register(Registries.CREATIVE_MODE_TAB, name, () -> {
            return Services.PLATFORM.creativeTab(after == null ? null : after.id())
                    .title(Component.translatable("itemGroup.cottagecoreliving." + name))
                    .icon(() -> new ItemStack(ModItems.decoration(icon)))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.COLLECTION_BOOK.get());
                        Collectibles.inCategory(category).forEach(e -> output.accept(e.item()));
                    })
                    .build();
        });
    }
}
