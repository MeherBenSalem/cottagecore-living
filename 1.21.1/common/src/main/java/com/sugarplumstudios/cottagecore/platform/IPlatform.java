package com.sugarplumstudios.cottagecore.platform;

import com.sugarplumstudios.cottagecore.network.OpenBookPacket;
import com.sugarplumstudios.cottagecore.network.TakePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/** What Cottagecore Living needs from the mod loader. One implementation per loader. */
public interface IPlatform {
    String name();

    Path configDir();

    /** Registers an object under cottagecoreliving:name in the given registry. */
    <R, T extends R> RegistryEntry<T> register(ResourceKey<? extends Registry<R>> registry, String name, Supplier<T> factory);

    /** A creative tab builder; {@code after} is the tab it follows (Forge orders tabs by it, Fabric by registration). */
    CreativeModeTab.Builder creativeTab(@Nullable ResourceLocation after);

    <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> factory, Block... blocks);

    /** Server only: opens the collection book on a player's client. */
    void sendToPlayer(ServerPlayer player, OpenBookPacket packet);

    /** Client only: asks the server to hand out decorations picked in the book. */
    void sendToServer(TakePacket packet);
}
