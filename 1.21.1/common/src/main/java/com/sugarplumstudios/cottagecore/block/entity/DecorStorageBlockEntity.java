package com.sugarplumstudios.cottagecore.block.entity;

import com.sugarplumstudios.cottagecore.block.Interactions;
import com.sugarplumstudios.cottagecore.block.StorageDecorBlock;
import com.sugarplumstudios.cottagecore.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Nine slots of storage behind a little lid. Works like a barrel, so hoppers and comparators work too. */
public class DecorStorageBlockEntity extends RandomizableContainerBlockEntity {
    public static final int SIZE = 9;

    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private final ContainerOpenersCounter openers = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            sound(level, pos, state, true);
            if (state.getBlock() instanceof StorageDecorBlock block) {
                block.setOpen(level, pos, state, true);
            }
        }

        @Override
        protected void onClose(Level level, BlockPos pos, BlockState state) {
            sound(level, pos, state, false);
            if (state.getBlock() instanceof StorageDecorBlock block) {
                block.setOpen(level, pos, state, false);
            }
        }

        @Override
        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int oldCount, int newCount) {
        }

        @Override
        protected boolean isOwnContainer(Player player) {
            AbstractContainerMenu menu = player.containerMenu;
            return menu instanceof ChestMenu chest && chest.getContainer() == DecorStorageBlockEntity.this;
        }
    };

    public DecorStorageBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STORAGE.get(), pos, state);
    }

    private static void sound(Level level, BlockPos pos, BlockState state, boolean open) {
        if (state.getBlock() instanceof StorageDecorBlock block && block.family().id().equals("jewelry_box") && open) {
            Interactions.chime(level, pos, 0.6F);
            return;
        }
        level.playSound(null, pos, open ? SoundEvents.BARREL_OPEN : SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS, 0.5F,
                level.random.nextFloat() * 0.1F + 1.3F);
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected Component getDefaultName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new ChestMenu(MenuType.GENERIC_9x1, id, inventory, this, 1);
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, items, registries);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        if (!tryLoadLootTable(tag)) {
            ContainerHelper.loadAllItems(tag, items, registries);
        }
    }

    @Override
    public void startOpen(Player player) {
        if (!remove && !player.isSpectator() && level != null) {
            openers.incrementOpeners(player, level, getBlockPos(), getBlockState());
        }
    }

    @Override
    public void stopOpen(Player player) {
        if (!remove && !player.isSpectator() && level != null) {
            openers.decrementOpeners(player, level, getBlockPos(), getBlockState());
        }
    }

    public void recheckOpen() {
        if (!remove && level != null) {
            openers.recheckOpeners(level, getBlockPos(), getBlockState());
        }
    }
}
