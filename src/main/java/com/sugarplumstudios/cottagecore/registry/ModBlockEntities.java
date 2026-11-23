package com.sugarplumstudios.cottagecore.registry;

import com.sugarplumstudios.cottagecore.CottagecoreLiving;
import com.sugarplumstudios.cottagecore.block.entity.DecorStorageBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CottagecoreLiving.MOD_ID);

    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<DecorStorageBlockEntity>> STORAGE = BLOCK_ENTITIES.register("storage",
            () -> BlockEntityType.Builder.of(DecorStorageBlockEntity::new, ModBlocks.storageBlocks()).build(null));

    private ModBlockEntities() {
    }
}
