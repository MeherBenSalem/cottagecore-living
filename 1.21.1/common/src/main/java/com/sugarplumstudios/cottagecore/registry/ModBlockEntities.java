package com.sugarplumstudios.cottagecore.registry;

import com.sugarplumstudios.cottagecore.block.entity.DecorStorageBlockEntity;
import com.sugarplumstudios.cottagecore.platform.RegistryEntry;
import com.sugarplumstudios.cottagecore.platform.Services;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    public static RegistryEntry<BlockEntityType<DecorStorageBlockEntity>> STORAGE;

    private ModBlockEntities() {
    }

    public static void init() {
        STORAGE = Services.PLATFORM.register(Registries.BLOCK_ENTITY_TYPE, "storage",
                () -> Services.PLATFORM.blockEntityType(DecorStorageBlockEntity::new, ModBlocks.storageBlocks()));
    }
}
