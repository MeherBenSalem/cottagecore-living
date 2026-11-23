package com.sugarplumstudios.cottagecore.block;

import com.sugarplumstudios.cottagecore.block.entity.DecorStorageBlockEntity;
import com.sugarplumstudios.cottagecore.catalog.DecorFamily;
import com.sugarplumstudios.cottagecore.catalog.DecorVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Trinket boxes, jewelry boxes and the picnic basket: nine slots, and the lid opens while in use. */
public class StorageDecorBlock extends DecorBlock implements EntityBlock {
    public StorageDecorBlock(DecorFamily family, DecorVariant variant) {
        super(family, variant);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DecorStorageBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND || player.isSecondaryUseActive()) {
            return super.use(state, level, pos, player, hand, hit);
        }
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof DecorStorageBlockEntity storage) {
            player.openMenu(storage);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public void setOpen(Level level, BlockPos pos, BlockState state, boolean open) {
        if (state.is(this) && state.getValue(OPEN) != open) {
            level.setBlock(pos, state.setValue(OPEN, open), Block.UPDATE_ALL);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof DecorStorageBlockEntity storage) {
            storage.recheckOpen();
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof DecorStorageBlockEntity storage) {
                Containers.dropContents(level, pos, storage);
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }
}
