package com.sugarplumstudios.cottagecore.registry;

import com.sugarplumstudios.cottagecore.CottagecoreLiving;
import com.sugarplumstudios.cottagecore.catalog.Category;
import com.sugarplumstudios.cottagecore.collection.Collectibles;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** One creative tab per collection: Interior, Garden and Seasonal. */
public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CottagecoreLiving.MOD_ID);

    public static final RegistryObject<CreativeModeTab> INTERIOR = TABS.register("interior", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.cottagecoreliving.interior"))
            .icon(() -> new ItemStack(ModItems.decoration("strawberry_teapot")))
            .displayItems((params, output) -> {
                output.accept(ModItems.COLLECTION_BOOK.get());
                Collectibles.inCategory(Category.INTERIOR).forEach(e -> output.accept(e.item()));
            })
            .build());

    public static final RegistryObject<CreativeModeTab> GARDEN = TABS.register("garden", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.cottagecoreliving.garden"))
            .icon(() -> new ItemStack(ModItems.decoration("strawberry_birdhouse")))
            .withTabsBefore(INTERIOR.getKey())
            .displayItems((params, output) -> {
                output.accept(ModItems.COLLECTION_BOOK.get());
                Collectibles.inCategory(Category.GARDEN).forEach(e -> output.accept(e.item()));
            })
            .build());

    public static final RegistryObject<CreativeModeTab> SEASONAL = TABS.register("seasonal", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.cottagecoreliving.seasonal"))
            .icon(() -> new ItemStack(ModItems.decoration("blossom_branch_vase")))
            .withTabsBefore(GARDEN.getKey())
            .displayItems((params, output) -> {
                output.accept(ModItems.COLLECTION_BOOK.get());
                Collectibles.inCategory(Category.SEASONAL).forEach(e -> output.accept(e.item()));
            })
            .build());

    private ModCreativeTabs() {
    }
}
